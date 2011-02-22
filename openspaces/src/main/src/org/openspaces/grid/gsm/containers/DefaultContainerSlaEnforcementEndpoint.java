package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
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
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
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

        List<GridServiceContainer> approvedContainers = ContainersSlaUtils.getContainersByZone(
                ContainersSlaUtils.getContainerZone(pu), pu.getAdmin());
        approvedContainers.removeAll(state.getContainersMarkedForShutdown(pu));

        return approvedContainers.toArray(new GridServiceContainer[approvedContainers.size()]);
    }

    public boolean isContainersPendingDeallocation()
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        return !state.getContainersMarkedForShutdown(pu).isEmpty();
    }

    public boolean enforceSla(ContainersSlaPolicy sla)
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        if (sla == null) {
            throw new IllegalArgumentException("sla cannot be null");
        }

        String[] zoneInContainerOptions = sla.getNewContainerConfig().getZones();

        String zone = ContainersSlaUtils.getContainerZone(pu);
        if (zoneInContainerOptions.length != 1 || !zoneInContainerOptions[0].equals(zone)) {
            throw new IllegalArgumentException("sla zone is " + Arrays.toString(zoneInContainerOptions)
                    + " and instead it should be " + zone);
        }

        if (sla.getAllocatedCapacity().getAgentUids().size() < sla.getMinimumNumberOfMachines()) {
            throw new IllegalArgumentException(
                    "Number of grid service agents must be at least minimum number of machines.");
        }

        try {
            enforceSlaInternal(sla);
        } catch (ConflictingOperationInProgressException e) {
            logger.info("Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",
                    e);
            return false; // try again next time
        } catch (NeedMoreMemoryException e) {
            logger.warn(e.getMessage());
            return false; // try again next time
        } catch (NeedMoreMachinesException e) {
            logger.warn(e.getMessage());
            return false; // try again next time
        } catch (NeedMoreCpuException e) {
            logger.warn(e.getMessage());
            return false; // try again next time
        }

        return isSlaReached(sla);
    }

    /**
     * @return true if reached exact target number of containers with specified container zone.
     */
    private boolean isSlaReached(ContainersSlaPolicy sla) {

        Iterable<GridServiceContainer> containers = Arrays.asList(getContainers());

        return ContainersSlaUtils.isCapacityMet(sla, containers)
                && state.getNumberOfContainersMarkedForShutdown(pu)== 0 
                && state.getNumberOfFutureContainers(pu) == 0;
    }

    public ProcessingUnit getId() {
        return pu;
    }

    private void enforceSlaInternal(final ContainersSlaPolicy sla) throws ConflictingOperationInProgressException,
            NeedMoreMachinesException, NeedMoreCpuException, NeedMoreMemoryException {

        cleanContainersMarkedForShutdown(pu);
        cleanFutureContainers();

        // mark for deallocation all containers that are not managed by an allocated agent
        String zone = ContainersSlaUtils.getContainerZone(pu);
        Collection<String> allocatedAgentUids = sla.getAllocatedCapacity().getAgentUids();
        for (GridServiceContainer container : ContainersSlaUtils.getContainersByZone(zone, pu.getAdmin())) {
            if (!allocatedAgentUids.contains(container.getGridServiceAgent().getUid())) {
                state.markForContainerForDeallocation(pu, container);
            }
        }

        // add or remove containers to the approved containers to meet SLA.

        List<GridServiceContainer> approvedContainers = ContainersSlaUtils.sortContainersInterleaved(getContainers());
        if (ContainersSlaUtils.isCapacityMet(sla, approvedContainers)) {
            // try to scale in, only if we are not in the process of starting new grid service
            // containers.
            if (state.getNumberOfFutureContainers(pu) == 0) {
                // try to scale in (mark container for shutdown) until SLA is met
                while (true) {
                    GridServiceContainer containerToRemove = findContainerForRemoval(sla, approvedContainers);
                    if (containerToRemove == null) {
                        break;
                    }
                    logger.info("Marking container " + ContainersSlaUtils.gscToString(containerToRemove) + " for shutdown. "+
                            "Containers="+Arrays.toString(getContainers())+ " "+
                            "#FutureContainers="+state.getFutureContainers(pu).size());
                    
                    state.markForContainerForDeallocation(pu, containerToRemove); 
                    approvedContainers.remove(containerToRemove);
                }
            }
        } else {
            // try to scale out until SLA is met
            while (true) {
                
                Collection<FutureGridServiceContainer> futureContainersCopy = state.getFutureContainers(pu);
                long memoryShortageInMB = ContainersSlaUtils.getFutureMemoryCapacityShortageInMB(sla,
                        approvedContainers, futureContainersCopy);
                double cpuCoresShortage = ContainersSlaUtils.getFutureNumberOfCpuCoresShortage(sla, approvedContainers,
                        futureContainersCopy);
                int machineShortage = ContainersSlaUtils.getFutureMachineShortage(sla, approvedContainers,
                        futureContainersCopy);
                
                if (logger.isDebugEnabled()) {
                    StringBuilder isMetExplanation = new StringBuilder();
                    if (memoryShortageInMB > 0) {
                        isMetExplanation.append("Containers SLA requires more " + memoryShortageInMB + "MB memory");
                    }
                    if (cpuCoresShortage > 0) {
                        isMetExplanation.append("Containers SLA requires more " + cpuCoresShortage + " CPU cores ");
                    }
                    if (machineShortage > 0) {
                        isMetExplanation.append("Containers SLA requires more " + machineShortage + " minimum machines");
                    }
                    if (isMetExplanation.length() > 0) {
                        logger.debug(isMetExplanation);
                    }
                }
                boolean isFutureCapacityMet = memoryShortageInMB <= 0 && cpuCoresShortage <= 0 && machineShortage <= 0;
                if (isFutureCapacityMet) {
                    break;
                }
                
                Set<Machine> futureMachinesHostingContainers = ContainersSlaUtils.getFutureMachinesHostingContainers(
                        approvedContainers, futureContainersCopy);
                // bring back a container that is marked for shutdown to the approved containers
                // list
                GridServiceContainer unmarkContainer = null;

                for (GridServiceContainer containerMarkedForShutdown : state.getContainersMarkedForShutdown(pu)) {
                    if (
                    // more memory is needed, meaning more containers are needed
                    (memoryShortageInMB > 0 ||
                    // or this container is on a machine with zero containers (would add cpu and
                    // machines)
                            !futureMachinesHostingContainers.contains(containerMarkedForShutdown.getMachine()))
                            &&

                            // unmark only containers that are managed by an approved agent
                            allocatedAgentUids.contains(containerMarkedForShutdown.getGridServiceAgent().getUid())) {

                        unmarkContainer = containerMarkedForShutdown;
                        break;
                    }
                }
                if (unmarkContainer != null) {
                    logger.info("Unmarking container " + ContainersSlaUtils.gscToString(unmarkContainer)
                            + " so it won't be shutdown.");
                    state.unmarkForShutdownContainer(pu, unmarkContainer);
                    approvedContainers.add(unmarkContainer);
                    continue;
                }

                // deploy a new container on an approved machine that
                // has the least number of containers.
                final GridServiceAgent gsa = findAgentForNewContainer(pu, sla);
                if (memoryShortageInMB > 0) {

                    //  more memory is needed
                 
                    if (logger.isInfoEnabled()) {
                        logger.info("Starting a new Grid Service Container on "
                                + ContainersSlaUtils.machineToString(gsa.getMachine())
                                + " due to memory shortage of " + memoryShortageInMB + "MB."
                                + "Machine is currently running "
                                + ContainersSlaUtils.gscsToString(gsa.getMachine()
                                    .getGridServiceContainers()
                                    .getContainers()));
                    }

                    addFutureContainer(sla, gsa);
                }
                else if (!futureMachinesHostingContainers.contains(gsa.getMachine())) {
                    // more cpu cores (or a new machine) is needed
                    // and this is an unused machine
                    
                    if (logger.isInfoEnabled()) {
                        logger.info("Starting a new Grid Service Container on "
                                + ContainersSlaUtils.machineToString(gsa.getMachine())
                                + " since more machines are needed for pu " + pu.getName() + " "
                                + "Machine is currently running "
                                + ContainersSlaUtils.gscsToString(gsa.getMachine()
                                    .getGridServiceContainers()
                                    .getContainers()));
                    }

                    addFutureContainer(sla, gsa);
                } else if (cpuCoresShortage > 0) {
                    throw new NeedMoreCpuException(cpuCoresShortage);
                } else if (machineShortage > 0) {
                    throw new NeedMoreMachinesException(machineShortage);
                }
            }
        }
    }

    private void addFutureContainer(final ContainersSlaPolicy sla, final GridServiceAgent gsa) {
        state.addFutureContainer(pu,
                ContainersSlaUtils.startGridServiceContainerAsync(
                    (InternalAdmin) pu.getAdmin(),
                    (InternalGridServiceAgent) gsa, 
                    sla.getNewContainerConfig(),
                    START_CONTAINER_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS));
    }

    private GridServiceContainer findContainerForRemoval(ContainersSlaPolicy sla, List<GridServiceContainer> containers) {

        for (GridServiceContainer container : containers) {

            List<GridServiceContainer> containersAfterScaleIn = new ArrayList<GridServiceContainer>(containers);

            containersAfterScaleIn.remove(container);

            if (ContainersSlaUtils.isCapacityMet(sla, containersAfterScaleIn)) {
                return container;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot remove container "
                            + ContainersSlaUtils.gscToString(container)
                            + " since "
                            + (ContainersSlaUtils.getMemoryCapacityShortageInMB(sla, containers) <= 0 ? " it would violate memory SLA." : ContainersSlaUtils.getNumberOfCpuCoresShortage(
                                    sla, containers) <= 0 ? " it would violate CPU sla." : ContainersSlaUtils.getMachineShortage(
                                    sla, containers) <= 0 ? " it would violate minimum number of machines." : "unknown"));
                }
            }
        }

        return null;
    }

    private GridServiceAgent findAgentForNewContainer(ProcessingUnit pu, ContainersSlaPolicy sla)
            throws ConflictingOperationInProgressException, NeedMoreMachinesException, NeedMoreMemoryException {

        List<GridServiceAgent> recommendedAgents = findAgentsForNewContainerSortByNumberOfContainersInZone(pu, sla);

        // Pick the most recommended agent.
        return recommendedAgents.get(0);
    }

    /**
     * finds all Grid Service Agents that have enough free space for a new grid service container.
     * The resultint agents are sorted by the number of containers (with the same zone) per machine.
     * 
     * @param pu
     * @param sla
     * @return
     * @throws ConflictingOperationInProgressException
     * @throws NeedMoreMemoryException
     */
    private List<GridServiceAgent> findAgentsForNewContainerSortByNumberOfContainersInZone(
            ProcessingUnit pu,
            ContainersSlaPolicy sla) throws ConflictingOperationInProgressException, NeedMoreMachinesException,
            NeedMoreMemoryException {

        List<GridServiceAgent> recommendedAgents = new ArrayList<GridServiceAgent>();

        boolean conflictingOperationInProgress = false;
        long requiredFreeMemoryInMB = sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB();
        final List<GridServiceContainer> containersByZone = ContainersSlaUtils.getContainersByZone(
                ContainersSlaUtils.getContainerZone(pu), pu.getAdmin());
        
        Collection<GridServiceAgent> allocatedAgents = 
            MachinesSlaUtils.getGridServiceAgentsFromUids(
                    sla.getAllocatedCapacity().getAgentUids(), 
                    pu.getAdmin());
        
        // calculate the allocated capacity that has not been used yet on all machines.
        AggregatedAllocatedCapacity allocatedCapacity = sla.getAllocatedCapacity();
        
        AllocatedCapacity capacityOfOneContainer = 
            new AllocatedCapacity( 
                    Fraction.ZERO /* 0 CPU Cores*/,
                    sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB() /* JVM Xmx settings */
                    );
  
        AggregatedAllocatedCapacity freeCapacity = 
            calculateRemainingFreeCapacity(
                    allocatedCapacity,containersByZone, capacityOfOneContainer);
        
        final List<GridServiceAgent> agentsSortedByNumberOfContainers = ContainersSlaUtils.sortAgentsByNumberOfContainers(
                allocatedAgents, containersByZone, state.getFutureContainers(pu));
        logger.debug("Considering " + agentsSortedByNumberOfContainers.size() + " agents to start a container on.");
        for (final GridServiceAgent agent : agentsSortedByNumberOfContainers) {

            final Machine machine = agent.getMachine();
            if (state.isFutureGridServiceContainerOnMachine(machine)) {
                // the reason we don't keep looking is that this machine might still have the least
                // number of containers, even though a container is being started on it.
                // so we'll just have to wait until the container is ready.
                throw new ConflictingOperationInProgressException();
            }
            
            if (!freeCapacity.getAgentCapacity(agent.getUid()).satisfies(capacityOfOneContainer)) {
                logger.debug(ContainersSlaUtils.machineToString(agent.getMachine())
                        + " does not have enough unallocated capacity. "
                        + "It has " + freeCapacity.getAgentCapacity(agent.getUid()) + " unallocated capacity. "
                        + "And a new container requires allocation of " + capacityOfOneContainer);
                continue;
            }
            
                
            final OperatingSystemStatistics operatingSystemStatistics = 
                machine.getOperatingSystem().getStatistics();

            // get total free system memory + cached (without sigar returns -1)
            long freeBytes = operatingSystemStatistics.getActualFreePhysicalMemorySizeInBytes();
            if (freeBytes <= 0) {
                // fallback - no sigar. Provides a pessimistic number since does not take into
                // account OS cache that can be allocated.
                freeBytes = operatingSystemStatistics.getFreePhysicalMemorySizeInBytes();
                if (freeBytes <= 0) {
                    // machine is probably going down. Blow everything up.
                    throw new ConflictingOperationInProgressException();
                }
            }

            final long freeInMB = MemoryUnit.MEGABYTES.convert(freeBytes, MemoryUnit.BYTES);

            if (freeInMB > requiredFreeMemoryInMB + sla.getReservedMemoryCapacityPerMachineInMB()) {
                recommendedAgents.add(agent);
            } else {
                logger.debug(ContainersSlaUtils.machineToString(agent.getMachine())
                        + " does not have enough free memory. "
                        + "It has only " + freeInMB + "MB free and required is "
                        + requiredFreeMemoryInMB + "MB plus reserved is "
                        + sla.getReservedMemoryCapacityPerMachineInMB() + "MB");
            }
        
        }

        if (recommendedAgents.size() == 0) {
            if (conflictingOperationInProgress) {
                throw new ConflictingOperationInProgressException();
            }
            throw new NeedMoreMemoryException(requiredFreeMemoryInMB);
        }

        return recommendedAgents;
    }

    /**
     * This method subtracts the memory used by existing containers from the total allocated capacity
     * for this processing unit.
     * @return the remaining free capacity for new containers allocation.  
     */
    private AggregatedAllocatedCapacity calculateRemainingFreeCapacity(
            AggregatedAllocatedCapacity allocatedCapacity,
            List<GridServiceContainer> containersByZone,
            AllocatedCapacity capacityOfOneContainer) {
        
        AggregatedAllocatedCapacity freeCapacity = allocatedCapacity ;
        for (GridServiceContainer container : containersByZone) {
            
            GridServiceAgent agent = container.getGridServiceAgent();
            // check that this container is on an allocated machine, and is not a candidate for deallocation
            if (allocatedCapacity.getAgentUids().contains(agent.getUid())) {
                
                //subtract the container memory capacity from the total allocated memory on this machine. 
                
                //we use subtractOrZero since GSA may start extra GSC due to false failure detection.
                //or if containers are marked for shutdown and have not been killed yet.
                freeCapacity = freeCapacity.subtractOrZero(agent.getUid(), capacityOfOneContainer);
            }
        }
        return freeCapacity;
    }

    /**
     * removes containers from the futureContainers list if the future is done (container started).
     */
    private void cleanFutureContainers() {

        List<FutureGridServiceContainer> futureContainers = state.removeAllDoneFutureContainers(pu);

        for (FutureGridServiceContainer future : futureContainers) {

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

        cleanFailedFutureContainers();
    }

    /**
     * kills and removes containers that are marked for shutdown and have no pu instances deployed
     * on them.
     */
    private void cleanContainersMarkedForShutdown(ProcessingUnit pu) {

        for (final GridServiceContainer container : state.getContainersMarkedForShutdown(pu)) {

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
    private static class ConflictingOperationInProgressException extends Exception {
    }

    @SuppressWarnings("serial")
    private static class NeedMoreMemoryException extends Exception {
        NeedMoreMemoryException(long missingCapacityInMB) {
            super("Cannot enforce Containers SLA since there are not enough machines available. "+
                  "Need more machines with " + missingCapacityInMB + "MB memory");
        
        }
    }

    @SuppressWarnings("serial")
    private static class NeedMoreCpuException extends Exception {
        NeedMoreCpuException(double cpuCoresShortage) {
            super("Cannot enforce Containers SLA since there are not enough machines available. "+
                  "Need more machines with " + cpuCoresShortage + "CPU cores");
        }
    }
    
    @SuppressWarnings("serial")
    private static class NeedMoreMachinesException extends Exception {
        NeedMoreMachinesException(int machineShortage) {
            super("Cannot enforce Containers SLA since there are not enough machines available. "+
                  "Need " + machineShortage + " more machines");
        }
    }


} 