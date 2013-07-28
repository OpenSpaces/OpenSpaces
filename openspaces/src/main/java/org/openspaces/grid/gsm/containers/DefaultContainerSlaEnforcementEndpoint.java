/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.containers.exceptions.ContainerNotDiscoveredException;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementPendingProcessingUnitDeallocationException;
import org.openspaces.grid.gsm.containers.exceptions.FailedToStartNewGridServiceContainersException;

import com.gigaspaces.grid.gsa.AgentProcessDetails;

class DefaultContainersSlaEnforcementEndpoint implements ContainersSlaEnforcementEndpoint {

	// enough time for the GSC also to register with the Lookup Service.
    private static final long START_CONTAINER_TIMEOUT_FAILURE_SECONDS = Long.getLong("org.openspaces.grid.start-container-timeout-seconds", 2*60L);
    private static final long START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS = START_CONTAINER_TIMEOUT_FAILURE_SECONDS + Long.getLong("org.openspaces.grid.wait-before-start-container-again-seconds", 1*60L);

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

    @Override
    public GridServiceContainer[] getContainers() {
        validateEndpointNotDestroyed(pu);

        Collection<GridServiceContainer> approvedContainers = ContainersSlaUtils.getContainersByZone(
                pu.getAdmin(),
                ContainersSlaUtils.getContainerZone(pu));
        approvedContainers.removeAll(state.getContainersMarkedForDeallocation(pu));

        return approvedContainers.toArray(new GridServiceContainer[approvedContainers.size()]);
    }

    public boolean isContainersPendingDeallocation() throws ContainersSlaEnforcementInProgressException{
        validateEndpointNotDestroyed(pu);
        return !state.getContainersMarkedForDeallocation(pu).isEmpty();
    }

    @Override
    public void enforceSla(ContainersSlaPolicy sla)
            throws ContainersSlaEnforcementInProgressException {
        
        validateEndpointNotDestroyed(pu);
        
        validateSla(sla, pu);

        checkAllUndiscoveredContainersAreNotRunning(sla);
        
        enforceSlaInternal(sla);
    }

    private static void validateSla(ContainersSlaPolicy sla, ProcessingUnit pu) {
        if (sla == null) {
            throw new IllegalArgumentException("sla cannot be null");
        }
        
        sla.validate();
        
        final String[] zoneInContainerOptions = sla.getNewContainerConfig().getZones();

        final String zone = ContainersSlaUtils.getContainerZone(pu);
        if (zoneInContainerOptions.length != 1 || !zoneInContainerOptions[0].equals(zone)) {
            throw new IllegalArgumentException("sla zone is " + Arrays.toString(zoneInContainerOptions)
                    + " and instead it should be " + zone);
        }
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }

    private void enforceSlaInternal(final ContainersSlaPolicy sla) throws ContainersSlaEnforcementInProgressException {

        cleanContainersMarkedForShutdown(pu);
        cleanFutureContainers(sla);
        
        markForDeallocationContainersOnUnallocatedMachines(sla);
        markForDeallocationContainersOnMachineWithAllocatedCapacityShortage(sla);
        startContainersOnMachineWithAllocatedCapacitySurplus(sla);

        if (!state.getContainersMarkedForDeallocation(pu).isEmpty()) {
            throw new ContainersSlaEnforcementPendingProcessingUnitDeallocationException(getProcessingUnit(), state.getContainersMarkedForDeallocation(pu)); 
        }
        
        if (state.getNumberOfContainersMarkedForShutdown(pu) > 0) {
            throw new ContainersSlaEnforcementInProgressException(pu, state.getNumberOfContainersMarkedForShutdown(pu) + " containers are pending shutdown.");
        }
        
        if (state.getNumberOfFutureContainers(pu) > 0) {
            throw new ContainersSlaEnforcementInProgressException(pu, "Containers still being started.");
        }
    }

    private void markForDeallocationContainersOnUnallocatedMachines(final ContainersSlaPolicy sla) {
        final Collection<String> allocatedAgentUids = sla.getClusterCapacityRequirements().getAgentUids();
        final String zone = ContainersSlaUtils.getContainerZone(pu);
        for (final GridServiceContainer container : ContainersSlaUtils.getContainersByZone(pu.getAdmin(), zone)) {
            if (!allocatedAgentUids.contains(container.getGridServiceAgent().getUid())) {
                if (logger.isInfoEnabled()) {
                    logger.info("Grid Service Container " + ContainersSlaUtils.gscToString(container)+ " "
                            + "is marked for shutdown since there is no allocation for pu " + pu.getName() + " on this machine. "
                            + "Machine is currently running "
                            + ContainersSlaUtils.gscsToString(container.getMachine()
                                .getGridServiceContainers()
                                .getContainers()));
                }
                state.markContainerForDeallocation(pu, container);
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
            List<GridServiceContainer> containersByZoneOnAgent = ContainersSlaUtils.getContainersByZoneOnAgentUid(pu.getAdmin(), zone, agentUid);
            final long containerMemoryInMB = sla.getNewContainerConfig().getMaximumMemoryCapacityInMB();
            for (final GridServiceContainer container : containersByZoneOnAgent) {
                
                if (!containersMarkedForDeallocation.contains(container)) {
                    
                    if (remainingAllocatedMemory >= containerMemoryInMB) {
                        logger.debug("Grid Service Container " + ContainersSlaUtils.gscToString(container)+ " "
                                + "is running and allocated for pu " + pu.getName());
                        remainingAllocatedMemory -= containerMemoryInMB;
                    }
                    else {
                        if (logger.isInfoEnabled()) {
                            logger.info("Grid Service Container " + ContainersSlaUtils.gscToString(container)+ " "
                                    + "is marked for shutdown since there is not enough memory allocated for pu " + pu.getName() + " "+
                                    "on this machine. "
                                    + "Allocated memory " + allocatedMemory + " "
                                    + "Containers on machine in zone " + zone + " " + ContainersSlaUtils.gscsToString(containersByZoneOnAgent) + " "
                                    + "All container on machine "
                                    + ContainersSlaUtils.gscsToString(container.getMachine()
                                        .getGridServiceContainers()
                                        .getContainers())
                                    + "Cluster allocated capacity: " + sla.getClusterCapacityRequirements().toDetailedString() + " "
                                    + "Container memory in MB: " + containerMemoryInMB);
                        }
                        state.markContainerForDeallocation(pu, container);
                    }
                }
            }
        }
    }

    /**
     * Looks for containers that should have been discovered since they are managed by the GSA
     * or containers that should have been removed since they are no longer managed by the GSA
     * @throws ContainerNotDiscoveredException 
     */
    private void checkAllUndiscoveredContainersAreNotRunning(final ContainersSlaPolicy sla) throws ContainerNotDiscoveredException {
        CapacityRequirementsPerAgent requirements = sla.getClusterCapacityRequirements();
        final Collection<String> allocatedAgentUids = requirements.getAgentUids();
        final String zone = ContainersSlaUtils.getContainerZone(pu);
        Admin admin = pu.getAdmin();
        for (String agentUid : allocatedAgentUids) {
            
            InternalGridServiceAgent agent = (InternalGridServiceAgent) admin.getGridServiceAgents().getAgentByUID(agentUid);
            if (agent == null) {
                throw new IllegalStateException("agent " + agentUid +" is not discovered");
            }
            
            for (InternalAgentGridComponent component : agent.getUnconfirmedRemovedAgentGridComponents()) {
                if (component instanceof GridServiceContainer) {
                    GridServiceContainer container = (GridServiceContainer) component;
                    if (ContainersSlaUtils.isContainerMatchesZone(container, zone)) {
                        ContainerNotDiscoveredException exception = new ContainerNotDiscoveredException(getProcessingUnit(), container);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Admin API undiscovered container validation failed", exception);
                        }
                        throw exception;
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
            final long containerMemoryInMB = sla.getNewContainerConfig().getMaximumMemoryCapacityInMB();
            int numberOfRunningContainers = 0;
            for (GridServiceContainer container : ContainersSlaUtils.getContainersByZoneOnAgentUid(pu.getAdmin(), zone, agentUid)) {
                
                if (!containersMarkedForDeallocation.contains(container)) {
                    numberOfRunningContainers++;
                }
            }
            int numberOfFutureContainers = 0;
            for (FutureGridServiceContainer futureContainer : futureContainers) {
                if (futureContainer.getGridServiceAgent().getUid().equals(agentUid)) {
                    numberOfFutureContainers++;
                }
            }
            
            GridServiceAgent agent = agents.getAgentByUID(agentUid);
            if (agent == null) {
                throw new IllegalStateException("agent " + agentUid +" is not discovered");
            }
            int numberOfContainersToStart = (int)Math.ceil(1.0*allocatedMemory/containerMemoryInMB) - numberOfRunningContainers - numberOfFutureContainers;
            if (numberOfContainersToStart > 0) {
              
                if (logger.isInfoEnabled()) {
                    logger.info(
                        "Starting " + numberOfContainersToStart + " containers = ceil(allocatedMemory/containerMemory) - runningContainers - futureContainers =" + 
                        "ceil(" + allocatedMemory +"/" + containerMemoryInMB + ") - " + numberOfRunningContainers + " - " + numberOfFutureContainers + "= "+ 
                        numberOfContainersToStart);
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("Containers on machine " + ContainersSlaUtils.machineToString(agent.getMachine()));
                    }
                }
                
                for (int i = 0 ; i < numberOfContainersToStart ; i++) {
                    startContainer(sla,agent);
                }
            }
        }
    }

    private long getMemoryInMB(CapacityRequirements capacityRequirements) {
        return capacityRequirements.getRequirement(new MemoryCapacityRequirement().getType()).getMemoryInMB();
    }

    private void startContainer(final ContainersSlaPolicy sla, final GridServiceAgent gsa) {
        state.addFutureContainer(pu,
                ContainersSlaUtils.startGridServiceContainerAsync(
                    (InternalAdmin) pu.getAdmin(),
                    (InternalGridServiceAgent) gsa, 
                    sla.getNewContainerConfig(),
                    logger,
                    START_CONTAINER_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS));
    }


    /**
     * removes containers from the futureContainers list if the future is done (container started).
     * @param sla 
     * @throws FailedToStartNewGridServiceContainersException 
     */
    private void cleanFutureContainers(ContainersSlaPolicy sla) throws FailedToStartNewGridServiceContainersException {

        FutureGridServiceContainer future;
        while((future = state.removeNextDoneFutureContainer(pu)) != null){
            Exception exception = null;

            try {
                GridServiceContainer container = future.get();
                if (container.isDiscovered()) {
                    logger.info("Container started succesfully " + ContainersSlaUtils.gscToString(container));
                }

            } catch (ExecutionException e) {
                // if runtime or error propagate exception "as-is"
                Throwable cause = e.getCause();
                if (cause instanceof TimeoutException || cause instanceof AdminException || cause instanceof InterruptedException) {
                    // expected exception
                    exception = e;
                }
                else {
                    throw new IllegalStateException("Unexpected Exception when starting a new container.",e);
                }
            } catch (TimeoutException e) {
                exception = e;
            }

            if (exception != null) {
                state.failedFutureContainer(future);
                FailedToStartNewGridServiceContainersException ex = new FailedToStartNewGridServiceContainersException(
                        future.getGridServiceAgent().getMachine(),
                        pu, 
                        exception);
                
                if (sla.isUndeploying()) {
                    logger.info("Ignoring failure to start new container since undeploying.",ex);
                }
                else {
                    throw ex;
                }
            }
        }

        cleanFailedFutureContainers();
    }

    /**
     * kills and removes containers that are marked for shutdown and have no pu instances deployed
     * on them.
     * @throws OperationInProgressException 
     */
    private void cleanContainersMarkedForShutdown(ProcessingUnit pu) {

        for (final GridServiceContainer container : state.getContainersMarkedForDeallocation(pu)) {

            boolean isContainerDiscovered = container.isDiscovered();
            
            if (!isContainerDiscovered) {
                // container kill completed
                state.unmarkForShutdownContainer(pu, container);
            }
            else if (container.getProcessingUnitInstances().length > 0) {
                // cannot kill container since it still has pu instances on it.
                logger.debug("Processing unit instances in container " + ContainersSlaUtils.gscToString(container) + " are still running.");
            }
            else {
                // kill container
                ((InternalAdmin) pu.getAdmin()).scheduleAdminOperation(new Runnable() {
                    public void run() {
                        boolean hasProcessingUnitInstances;
                        try {
                            hasProcessingUnitInstances = ((InternalGridServiceContainer)container).hasProcessingUnitInstances();
                        } catch (AdminException e) {
                            logger.info("Cannot determine number of processing unit instances running on conatiner " + ContainersSlaUtils.gscToString(container),e);
                            return;
                        }
                        
                        if (hasProcessingUnitInstances) {
                            logger.debug("Processing unit instances in container " + ContainersSlaUtils.gscToString(container) + " are shutting down. " +
                                    "Suspect instance uids:"+Arrays.toString(((InternalGridServiceContainer)container).getUnconfirmedRemovedProcessingUnitInstancesUid()));
                        }
                        else {
                            logger.info("Killing container " + ContainersSlaUtils.gscToString(container) + " since it is not running any processing unit instances.");
                            try {
                                container.kill();
                            }
                            catch (AdminException e) {
                                logger.info("Cannot kill container " + ContainersSlaUtils.gscToString(container),e);
                            }
                            catch (IllegalArgumentException e) {
                                //GsaImpl throws IllegalArgumentException instead of AdminException in case the process no longer exists
                                logger.info("Cannot kill container " + ContainersSlaUtils.gscToString(container),e);
                            }
                        }
                    }
                });
            }
        }
    }

    private void validateEndpointNotDestroyed(ProcessingUnit pu) {

        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        
        if (state.isProcessingUnitDestroyed(pu)) {

            throw new IllegalStateException("endpoint destroyed");
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

    private void terminateOrphanContainersOfAgent(final GridServiceAgent agent) {
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

        for (FutureGridServiceContainer future : state.getFutureContainers()) {
            if (future.getGridServiceAgent().equals(agent) && future.isStarted()) {
                try {
                    agentIds.remove(future.getAgentId());
                } catch (ExecutionException e) {
                    // ignore
                } catch (TimeoutException e) {
                    // ignore
                }
            }
        }
        
        ((InternalAdmin) pu.getAdmin()).scheduleAdminOperation(new Runnable() {
            public void run() {
        
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
        });
    }

} 
