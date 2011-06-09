package org.openspaces.grid.gsm.containers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

import com.gigaspaces.grid.gsa.AgentProcessDetails;

class DefaultContainersSlaEnforcementEndpoint implements ContainersSlaEnforcementEndpoint {

    private static final int START_CONTAINER_TIMEOUT_FAILURE_SECONDS = 60;
    private static final int START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS = 120;

    private final ProcessingUnit pu;
    private final Log logger;
    private ContainersSlaEnforcementState state;

    public DefaultContainersSlaEnforcementEndpoint(ProcessingUnit pu, ContainersSlaEnforcementState state) {
        this.pu = pu;
        this.logger = 
            new LogPerProcessingUnit(
                new SingleThreadedPollingLog(
                        LogFactory.getLog(DefaultContainersSlaEnforcementEndpoint.class)), 
                pu);
        this.state = state;
    }

    public GridServiceContainer[] getContainers() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);

        Collection<GridServiceContainer> approvedContainers = ContainersSlaUtils.getContainersByZone(
                pu.getAdmin(),
                ContainersSlaUtils.getContainerZone(pu));
        approvedContainers.removeAll(state.getContainersMarkedForDeallocation(pu));

        return approvedContainers.toArray(new GridServiceContainer[approvedContainers.size()]);
    }

    public boolean isContainersPendingDeallocation()
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        return !state.getContainersMarkedForDeallocation(pu).isEmpty();
    }

    public boolean enforceSla(ContainersSlaPolicy sla)
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        if (sla == null) {
            throw new IllegalArgumentException("sla cannot be null");
        }

        if (sla.getNewContainerConfig().getMaximumMemoryCapacityInMB() <=0) {
            throw new IllegalStateException("container memory capacity cannot be zero.");
        }
        
        if (sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB() <=0) {
            throw new IllegalStateException("container memory capacity cannot be zero.");
        }
        
        String[] zoneInContainerOptions = sla.getNewContainerConfig().getZones();

        String zone = ContainersSlaUtils.getContainerZone(pu);
        if (zoneInContainerOptions.length != 1 || !zoneInContainerOptions[0].equals(zone)) {
            throw new IllegalArgumentException("sla zone is " + Arrays.toString(zoneInContainerOptions)
                    + " and instead it should be " + zone);
        }

        try {
            enforceSlaInternal(sla);
            return true;
        } catch (OperationInProgressException e) {
            logger.info("Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",
                    e);
            return false; // try again next time
        }

    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }

    private void enforceSlaInternal(final ContainersSlaPolicy sla) throws OperationInProgressException {

        cleanContainersMarkedForShutdown(pu);
        cleanFutureContainers();
        
        markForDeallocationContainersOnUnallocatedMachines(sla);
        markForDeallocationContainersOnMachineWithAllocatedCapacityShortage(sla);
        startContainersOnMachineWithAllocatedCapacitySurplus(sla);
        
        if (state.getNumberOfContainersMarkedForShutdown(pu) > 0) {
            throw new OperationInProgressException();
        }
        
        if (state.getNumberOfFutureContainers(pu) > 0) {
            throw new OperationInProgressException();
        }
        
    }

    private void markForDeallocationContainersOnUnallocatedMachines(final ContainersSlaPolicy sla) {
        {
        final Collection<String> allocatedAgentUids = sla.getClusterCapacityRequirements().getAgentUids();
        final String zone = ContainersSlaUtils.getContainerZone(pu);
        for (final GridServiceContainer container : ContainersSlaUtils.getContainersByZone(pu.getAdmin(), zone)) {
            if (!allocatedAgentUids.contains(container.getGridServiceAgent().getUid())) {
                if (logger.isInfoEnabled()) {
                    logger.info("Grid Service Container " + ContainersSlaUtils.gscToString(container)+ " "
                            + "is marked for shutdown since there is no allocation for pu" + pu.getName() + " on this machine. "
                            + "Machine is currently running "
                            + ContainersSlaUtils.gscsToString(container.getMachine()
                                .getGridServiceContainers()
                                .getContainers()));
                }
                state.markContainerForDeallocation(pu, container);
            }
        }
        }
    }

    private void markForDeallocationContainersOnMachineWithAllocatedCapacityShortage(final ContainersSlaPolicy sla) {
        final Collection<String> allocatedAgentUids = sla.getClusterCapacityRequirements().getAgentUids();
        final String zone = ContainersSlaUtils.getContainerZone(pu);
        // mark for deallocation all containers that do not fit to the allocated memory on agent
        final Collection<GridServiceContainer> containersMarkedForDeallocation = state.getContainersMarkedForDeallocation(pu);
        for (final String agentUid : allocatedAgentUids) {
            final long allocatedMemory = getMemoryInMB(sla.getClusterCapacityRequirements().getAgentCapacity(agentUid));
            long remainingAllocatedMemory = allocatedMemory;
            for (final GridServiceContainer container : ContainersSlaUtils.getContainersByZoneOnAgentUid(pu.getAdmin(), zone, agentUid)) {
                final long containerMemoryInMB = ContainersSlaUtils.getMemoryInMB(container);
                if (!containersMarkedForDeallocation.contains(container)) {
                    
                    if (remainingAllocatedMemory >= containerMemoryInMB) {
                        remainingAllocatedMemory -= containerMemoryInMB;
                    }
                    else {
                        if (logger.isInfoEnabled()) {
                            logger.info("Grid Service Container " + ContainersSlaUtils.gscToString(container)+ " "
                                    + "is marked for shutdown since there is not enough memory allocated for pu" + pu.getName() + " "+
                                    "on this machine. "
                                    + "Allocated memory " + allocatedMemory + " "  
                                    + "Machine is currently running "
                                    + ContainersSlaUtils.gscsToString(container.getMachine()
                                        .getGridServiceContainers()
                                        .getContainers()));
                        }
                        state.markContainerForDeallocation(pu, container);
                    }
                }
            }
        }
    }

    private void startContainersOnMachineWithAllocatedCapacitySurplus(final ContainersSlaPolicy sla) {
        final String zone = ContainersSlaUtils.getContainerZone(pu);
        final Collection<String> allocatedAgentUids = sla.getClusterCapacityRequirements().getAgentUids();
        Collection<GridServiceContainer> containersMarkedForDeallocation = state.getContainersMarkedForDeallocation(pu);
        Collection<FutureGridServiceContainer> futureContainers = state.getFutureContainers(pu);
        GridServiceAgents agents = pu.getAdmin().getGridServiceAgents();
        for (String agentUid : allocatedAgentUids) {
            
            long allocatedMemory = getMemoryInMB(sla.getClusterCapacityRequirements().getAgentCapacity(agentUid));
            for (GridServiceContainer container : ContainersSlaUtils.getContainersByZoneOnAgentUid(pu.getAdmin(), zone, agentUid)) {
                final long containerMemoryInMB = ContainersSlaUtils.getMemoryInMB(container);
                if (!containersMarkedForDeallocation.contains(container)) {
                    allocatedMemory -= containerMemoryInMB;
                }
            }
            
            for (FutureGridServiceContainer futureContainer : futureContainers) {
                if (futureContainer.getGridServiceAgent().getUid().equals(agentUid)) {
                    final long containerMemoryInMB = futureContainer.getGridServiceContainerConfig().getMaximumMemoryCapacityInMB();
                    allocatedMemory -= containerMemoryInMB;
                }
            }
            
            final long containerMemoryInMB = sla.getNewContainerConfig().getMaximumMemoryCapacityInMB(); 
            while (allocatedMemory >= containerMemoryInMB) {
                allocatedMemory -= containerMemoryInMB;
                GridServiceAgent agent = agents.getAgentByUID(agentUid);
                if (agent == null) {
                    throw new IllegalStateException("agent " + agentUid +" is not discovered");
                }
                startContainer(sla,agent);
            }
        }
    }

    private long getMemoryInMB(CapacityRequirements capacityRequirements) {
        return capacityRequirements.getRequirement(new MemoryCapacityRequirement().getType()).getMemoryInMB();
    }

    private void startContainer(final ContainersSlaPolicy sla, final GridServiceAgent gsa) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting a new Grid Service Container on "
                    + ContainersSlaUtils.machineToString(gsa.getMachine())
                    + " since more machines are needed for pu " + pu.getName() + " "
                    + "Machine is currently running "
                    + ContainersSlaUtils.gscsToString(gsa.getMachine()
                        .getGridServiceContainers()
                        .getContainers()));
        }
        
        state.addFutureContainer(pu,
                ContainersSlaUtils.startGridServiceContainerAsync(
                    (InternalAdmin) pu.getAdmin(),
                    (InternalGridServiceAgent) gsa, 
                    sla.getNewContainerConfig(),
                    START_CONTAINER_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS));
    }


    /**
     * removes containers from the futureContainers list if the future is done (container started).
     */
    private void cleanFutureContainers() {

        List<FutureGridServiceContainer> futureContainers = state.removeAllDoneFutureContainers(pu);

        for (FutureGridServiceContainer future : futureContainers) {

            if (future.isDone()) {
                Exception exception = null;
    
                try {
                    GridServiceContainer container = future.get();
                    if (container.isDiscovered()) {
                        logger.info("Container started succesfully " + ContainersSlaUtils.gscToString(container));
                    }
    
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof AdminException) {
                        exception = (AdminException) e.getCause();
                    } else {
                        throw new IllegalStateException("Unexpected runtime exception", e);
                    }
                } catch (TimeoutException e) {
                    exception = e;
                }
    
                if (exception != null) {
                    final String errorMessage = "Failed to start container on machine "
                            + ContainersSlaUtils.machineToString(future.getGridServiceAgent().getMachine());
                    logger.warn(errorMessage, exception);
                    state.failedFutureContainer(future);
                }
            }
        }

        cleanFailedFutureContainers();
    }

    /**
     * kills and removes containers that are marked for shutdown and have no pu instances deployed
     * on them.
     */
    private void cleanContainersMarkedForShutdown(ProcessingUnit pu) {

        for (final GridServiceContainer container : state.getContainersMarkedForDeallocation(pu)) {

            if (!container.isDiscovered()) {
                // container kill completed
                state.unmarkForShutdownContainer(pu, container);
            }

            else if (container.getProcessingUnitInstances().length == 0) {
                // kill container
                ((InternalAdmin) pu.getAdmin()).scheduleAdminOperation(new Runnable() {

                    public void run() {
                        container.kill();
                    }
                });
            } else {
                // cannot kill container since it still has pu instances on it.
            }
        }
    }

    private void validateEndpointNotDestroyed(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {

        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        
        if (state.isProcessingUnitDestroyed(pu)) {

            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }

    private void cleanFailedFutureContainers() {

        for (FutureGridServiceContainer future : state.getFailedFutureContainers()) {

            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp().getTime()) / 1000);
            GridServiceAgent agent = future.getGridServiceAgent();
            if (!agent.isDiscovered()) {
                logger.info("Forgetting failure to start container on machine "
                        + ContainersSlaUtils.machineToString(agent.getMachine()) + " that occured " + passedSeconds
                        + " seconds ago since grid service agent no longer exists.");
                state.removeFailedFuture(future);
            } else {
                terminateOrphanContainersOfAgent(agent);
                if (passedSeconds > START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS) {
                    logger.info("Forgetting failure to start container on machine "
                            + ContainersSlaUtils.machineToString(agent.getMachine()) + " that occured " + passedSeconds
                            + " seconds ago due to timeout.");
                    state.removeFailedFuture(future);
                }
            }
        }
    }

    private void terminateOrphanContainersOfAgent(GridServiceAgent agent) {
        final Set<Integer> agentIds = new HashSet<Integer>();
        // add all agent's containers process ids.
        for (final AgentProcessDetails processDetails : agent.getProcessesDetails()) {
            if (processDetails.getServiceType().toLowerCase().equals("gsc")) {
                agentIds.add(processDetails.getAgentId());
            }
        }
        // remove all agent's containers process ids that registered with lus.
        for (final GridServiceContainer container : agent.getAdmin().getGridServiceContainers()) {
            if (container.getGridServiceAgent().equals(agent)) {
                agentIds.remove(container.getAgentId());
            }
        }

        for (final int agentId : agentIds) {
            try {
                agent.killByAgentId(agentId);
                logger.warn("Terminated orphan container that did not register with lookup service on machine "
                        + ContainersSlaUtils.machineToString(agent.getMachine()) + " agentId=" + agentId);
            } catch (final AdminException e) {
                logger.warn("Error terminating orphan container that did not register with lookup service on machine "
                        + ContainersSlaUtils.machineToString(agent.getMachine()) + " agentId=" + agentId, e);
            }
        }
    }
    
    @SuppressWarnings("serial")
    private static class OperationInProgressException extends Exception {

        private static final long serialVersionUID = -5017788679551801723L;
    }
  
} 