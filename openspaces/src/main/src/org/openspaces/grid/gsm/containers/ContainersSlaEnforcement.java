package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

import com.gigaspaces.grid.gsa.AgentProcessDetails;

/**
 * Starts and shutdowns grid service container based on the requested {@link ContainersSlaPolicy}
 * Use {@link ContainersSlaEnforcement#createContainersAdminService() to enforce an SLA for a specific container zone.
 * 
 * @see ContainersSlaEnforcementEndpoint
 * @see ContainersSlaPolicy
 * @author itaif
 *
 */
public class ContainersSlaEnforcement implements
        ServiceLevelAgreementEnforcement<ContainersSlaPolicy, ProcessingUnit, ContainersSlaEnforcementEndpoint> {

    private static final Log logger = LogFactory.getLog(ContainersSlaEnforcement.class);
    
    private static final int START_CONTAINER_TIMEOUT_FAILURE_SECONDS = 60;
    private static final int START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS = 120;

    // State shared by all endpoints.
    private final Map<ProcessingUnit, List<GridServiceContainer>> containersMarkedForShutdownPerProcessingUnit;
    private final Map<ProcessingUnit, List<FutureGridServiceContainer>> futureContainersPerProcessingUnit;
    private final List<FutureGridServiceContainer> failedFutureContainers;

    private final InternalAdmin admin;
    private final Map<ProcessingUnit, ContainersSlaEnforcementEndpoint> endpoints;

    public ContainersSlaEnforcement(Admin admin) {
        this.admin = (InternalAdmin) admin;
        this.endpoints = new HashMap<ProcessingUnit, ContainersSlaEnforcementEndpoint>();
        this.containersMarkedForShutdownPerProcessingUnit = new HashMap<ProcessingUnit, List<GridServiceContainer>>();
        this.futureContainersPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureGridServiceContainer>>();
        this.failedFutureContainers = new ArrayList<FutureGridServiceContainer>();
    }

    /**
     * 
     * @return a service that continuously maintains the specified number of containers for the
     *         specified pu.
     */
    public ContainersSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {

        if (!isEndpointDestroyed(pu)) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for the pu already exists.");
        }

        ProcessingUnit otherPu1 = ContainersSlaUtils.findProcessingUnitWithSameName(endpoints.keySet(), pu);
        if (otherPu1 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same name already exists.");
        }

        ProcessingUnit otherPu2 = ContainersSlaUtils.findProcessingUnitWithSameZone(endpoints.keySet(), pu);
        if (otherPu2 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same (containers) zone already exists: "
                    + otherPu2.getName());
        }

        ContainersSlaEnforcementEndpoint endpoint = new DefaultContainersSlaEnforcementEndpoint(pu);
        endpoints.put(pu, endpoint);
        containersMarkedForShutdownPerProcessingUnit.put(pu, new ArrayList<GridServiceContainer>());
        futureContainersPerProcessingUnit.put(pu, new ArrayList<FutureGridServiceContainer>());

        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {
        containersMarkedForShutdownPerProcessingUnit.remove(pu);
        futureContainersPerProcessingUnit.remove(pu);
        endpoints.remove(pu);
    }

    public void destroy() {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }

    class DefaultContainersSlaEnforcementEndpoint implements ContainersSlaEnforcementEndpoint {
        
        private final ProcessingUnit pu;
        private final Log logger;
        public DefaultContainersSlaEnforcementEndpoint(ProcessingUnit pu) {
            this.pu = pu;
            this.logger = new LogPerProcessingUnit(ContainersSlaEnforcement.logger,pu);
        }

        public GridServiceContainer[] getContainers() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            validateEndpointNotDestroyed(pu);

            List<GridServiceContainer> approvedContainers = ContainersSlaUtils.getContainersByZone(
                    ContainersSlaUtils.getContainerZone(pu), admin);
            approvedContainers.removeAll(containersMarkedForShutdownPerProcessingUnit.get(pu));

            return approvedContainers.toArray(new GridServiceContainer[approvedContainers.size()]);
        }

        public GridServiceContainer[] getContainersPendingShutdown()
                throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            ContainersSlaEnforcement.this.validateEndpointNotDestroyed(pu);
            List<GridServiceContainer> containers = ContainersSlaEnforcement.this.containersMarkedForShutdownPerProcessingUnit.get(pu);
            return containers.toArray(new GridServiceContainer[containers.size()]);
        }

        public boolean enforceSla(ContainersSlaPolicy sla)
                throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            ContainersSlaEnforcement.this.validateEndpointNotDestroyed(pu);
            if (sla == null) {
                throw new IllegalArgumentException("sla cannot be null");
            }

            String[] zoneInContainerOptions = sla.getNewContainerConfig().getZones();

            String zone = ContainersSlaUtils.getContainerZone(pu);
            if (zoneInContainerOptions.length != 1 || !zoneInContainerOptions[0].equals(zone)) {
                throw new IllegalArgumentException("sla zone is " + Arrays.toString(zoneInContainerOptions)
                        + " and instead it should be " + zone);
            }

            if (sla.getGridServiceAgents().length < sla.getMinimumNumberOfMachines()) {
                throw new IllegalArgumentException ("Number of grid service agents must be at least minimum number of machines.");
            }
            
            try {
                enforceSlaInternal(sla);
            } catch (ConflictingOperationInProgressException e) {
                logger.info(
                        "Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",
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
                    && containersMarkedForShutdownPerProcessingUnit.get(pu).size() == 0
                    && futureContainersPerProcessingUnit.get(pu).size() == 0;
        }

        public ProcessingUnit getId() {
            return pu;
        }

        private void enforceSlaInternal(final ContainersSlaPolicy sla) throws ConflictingOperationInProgressException,
                NeedMoreMachinesException, NeedMoreCpuException, NeedMoreMemoryException {

            cleanContainersMarkedForShutdown();
            cleanFutureContainers();

            List<GridServiceContainer> containersMarkedForShutdown = containersMarkedForShutdownPerProcessingUnit.get(pu);
            List<FutureGridServiceContainer> futureContainers = futureContainersPerProcessingUnit.get(pu);

            // mark for shutdown all containers that are not managed by an approved agent
            String zone = ContainersSlaUtils.getContainerZone(pu);
            List<GridServiceAgent> approvedAgents = Arrays.asList(sla.getGridServiceAgents());
            for (GridServiceContainer container : ContainersSlaUtils.getContainersByZone(zone, admin)) {
                if (!approvedAgents.contains(container.getGridServiceAgent()) && !containersMarkedForShutdown.contains(container)) {
                    containersMarkedForShutdown.add(container);
                }
            }

            // add or remove containers to the approved containers to meet SLA.
            
            List<GridServiceContainer> approvedContainers = ContainersSlaUtils.sortContainersInterleaved(getContainers());
            if (ContainersSlaUtils.isCapacityMet(sla, approvedContainers)) {
                // try to scale in, only if we are not in the process of starting new grid service
                // containers.
                if (futureContainers.size() == 0) {
                    // try to scale in (mark container for shutdown) until SLA is met
                    while (true) {
                        GridServiceContainer containerToRemove = findContainerForRemoval(sla, approvedContainers);
                        if (containerToRemove == null) {
                            break;
                        }
                        logger.info("Marking container " + ContainersSlaUtils.gscToString(containerToRemove) +" for shutdown.");
                        containersMarkedForShutdown.add(containerToRemove);
                        approvedContainers.remove(containerToRemove);
                    }
                }
            } else {
                // try to scale out until SLA is met
                while (true) {
                long memoryShortageInMB = ContainersSlaUtils.getFutureMemoryCapacityShortageInMB(sla, approvedContainers, futureContainers);
                double cpuCoresShortage = ContainersSlaUtils.getFutureNumberOfCpuCoresShortage(sla, approvedContainers, futureContainers);
                int machineShortage = ContainersSlaUtils.getFutureMachineShortage(sla, approvedContainers, futureContainers);
                
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

                Set<Machine> futureMachinesHostingContainers = ContainersSlaUtils.getFutureMachinesHostingContainers(approvedContainers,futureContainers);
                    // bring back a container that is marked for shutdown to the approved containers
                    // list
                    GridServiceContainer unmarkContainer = null;

                    for (GridServiceContainer containerMarkedForShutdown : containersMarkedForShutdown) {
                        if (
                        // more memory is needed, meaning more containers are needed
                        (memoryShortageInMB > 0 || 
                        // or this container is on a machine with zero containers (would add cpu and machines)        
                        !futureMachinesHostingContainers.contains(containerMarkedForShutdown.getMachine())) &&
                        
                        // unmark only containers that are managed by an approved agent
                        approvedAgents.contains(containerMarkedForShutdown.getGridServiceAgent())) {
                            
                            unmarkContainer = containerMarkedForShutdown;
                            break;
                        }
                    }
                    if (unmarkContainer != null) {
                        logger.info("Unmarking container " + ContainersSlaUtils.gscToString(unmarkContainer) + " so it won't be shutdown.");
                        containersMarkedForShutdownPerProcessingUnit.remove(unmarkContainer);
                        approvedContainers.add(unmarkContainer);
                        continue;
                    }

                    // deploy a new container on an approved machine that 
                    // has the least number of containers.
                    final GridServiceAgent gsa = findAgentForNewContainer(pu, sla);
                    if (
                        // more memory is needed
                        memoryShortageInMB > 0 ||
                        
                        // more cpu cores (or a new machine) is needed 
                        // and this is an unused machine        
                        !futureMachinesHostingContainers.contains(gsa.getMachine())) {
                            
                    logger.info("Starting a new Grid Service Container on " + ContainersSlaUtils.machineToString(gsa.getMachine()));
                    futureContainers.add(ContainersSlaUtils.startGridServiceContainerAsync(admin,
                            (InternalGridServiceAgent) gsa, sla.getNewContainerConfig(),
                            START_CONTAINER_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS));
                    }
                    else if (cpuCoresShortage > 0) {
                                throw new NeedMoreCpuException(cpuCoresShortage);
                    }
                    else if (machineShortage > 0) {
                                throw new NeedMoreMachinesException(machineShortage);                       
                    }
                }
            }
        }

        private GridServiceContainer findContainerForRemoval(ContainersSlaPolicy sla,
                List<GridServiceContainer> containers) {

            for (GridServiceContainer container : containers) {

                List<GridServiceContainer> containersAfterScaleIn = new ArrayList<GridServiceContainer>(containers);

                containersAfterScaleIn.remove(container);

                if (ContainersSlaUtils.isCapacityMet(sla, containersAfterScaleIn)) {
                    return container;
                }
                else {
                    if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Cannot remove container " + ContainersSlaUtils.gscToString(container) + " since " +
                            (ContainersSlaUtils.getMemoryCapacityShortageInMB(sla, containers) <= 0 ? " it would violate memory SLA." :
                             ContainersSlaUtils.getNumberOfCpuCoresShortage(sla, containers) <= 0 ? " it would violate CPU sla." :
                             ContainersSlaUtils.getMachineShortage(sla, containers) <= 0 ? " it would violate minimum number of machines." : "unknown"));
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
         * @param pu
         * @param sla
         * @return
         * @throws ConflictingOperationInProgressException
         * @throws NeedMoreMemoryException 
         */
        private List<GridServiceAgent> findAgentsForNewContainerSortByNumberOfContainersInZone(ProcessingUnit pu,
                ContainersSlaPolicy sla) throws ConflictingOperationInProgressException, NeedMoreMachinesException, NeedMoreMemoryException {

            List<GridServiceAgent> recommendedAgents = new ArrayList<GridServiceAgent>();

            boolean conflictingOperationInProgress = false;
            long requiredFreeMemoryInMB = sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB();
            final List<GridServiceContainer> containersByZone = ContainersSlaUtils.getContainersByZone(
                    ContainersSlaUtils.getContainerZone(pu), admin);
            final List<GridServiceAgent> agentsSortedByNumberOfContainers = ContainersSlaUtils.sortAgentsByNumberOfContainers(
                    sla.getGridServiceAgents(), containersByZone);
            logger.debug("Considering " + agentsSortedByNumberOfContainers.size() + " agents to start a container on.");
            for (final GridServiceAgent gsa : agentsSortedByNumberOfContainers) {

                final Machine machine = gsa.getMachine();
                if (isFutureGridServiceContainerOnMachine(machine)) {
                    // the reason we don't keep looking is that this machine might still have the least
                    // number of containers, even though a container is being started on it.
                    // so we'll just have to wait until the container is ready.
                    throw new ConflictingOperationInProgressException();
                }

                final OperatingSystemStatistics operatingSystemStatistics = machine.getOperatingSystem().getStatistics();

                // get total free system memory + cached (without sigar returns -1)
                long freeBytes = operatingSystemStatistics.getActualFreePhysicalMemorySizeInBytes(); 
                if (freeBytes <= 0) {
                    // fallback - no sigar. Provides a pessimistic number since does not take into account OS cache that can be allocated.
                    freeBytes = operatingSystemStatistics.getFreePhysicalMemorySizeInBytes();
                    if (freeBytes <= 0) {
                        // machine is probably going down. Blow everything up.
                        throw new ConflictingOperationInProgressException(); 
                    }
                }
                
                final long freeInMB = MemoryUnit.MEGABYTES.convert(freeBytes,MemoryUnit.BYTES);

                if (freeInMB > requiredFreeMemoryInMB + sla.getReservedMemoryCapacityPerMachineInMB()) {
                    recommendedAgents.add(gsa);
                }
                else {
                    logger.debug(ContainersSlaUtils.machineToString(gsa.getMachine()) + " does not have enough free memory. It has only " + freeInMB + "MB free and required is " + requiredFreeMemoryInMB + "MB plus reserved is " + sla.getReservedMemoryCapacityPerMachineInMB()+"MB");
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
         * removes containers from the futureContainers list if the future is done (container started).
         */
        private void cleanFutureContainers() {
            List<FutureGridServiceContainer> list = futureContainersPerProcessingUnit.get(pu);
            final Iterator<FutureGridServiceContainer> iterator = list.iterator();
            while (iterator.hasNext()) {
                FutureGridServiceContainer future = iterator.next();

                if (future.isDone()) {

                    iterator.remove();

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
                        failedFutureContainers.add(future);
                    }
                }
            }

            cleanFailedFutureContainers();

        }

        /**
         * kills and removes containers that are marked for shutdown and have no pu instances deployed on them.
         */
        private void cleanContainersMarkedForShutdown() {

            final List<GridServiceContainer> containersPendingRelocation = containersMarkedForShutdownPerProcessingUnit.get(pu);
            final Iterator<GridServiceContainer> iterator = containersPendingRelocation.iterator();
            while (iterator.hasNext()) {
                final GridServiceContainer container = iterator.next();

                if (!container.isDiscovered()) {
                    //container kill completed
                    iterator.remove();
                }

                else if (container.getProcessingUnitInstances().length == 0) {
                    //kill container
                    ((InternalAdmin)pu.getAdmin()).scheduleAdminOperation(new Runnable() {

                        public void run() {
                            container.kill();
                        }
                    });
                }
                else {
                    // cannot kill container since it still has pu instances on it.
                }
            }
        }

    }


    /**
     * @return true if there is pending grid service container allocation on the machine.
     */
    private boolean isFutureGridServiceContainerOnMachine(Machine machine) {

        for (List<FutureGridServiceContainer> futures : this.futureContainersPerProcessingUnit.values()) {
            for (FutureGridServiceContainer future : futures) {
                if (future.getGridServiceAgent().getMachine().equals(machine)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void validateEndpointNotDestroyed(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {

        if (isEndpointDestroyed(pu)) {

            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }

    private boolean isEndpointDestroyed(ProcessingUnit pu) {

        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        return !endpoints.containsKey(pu) || containersMarkedForShutdownPerProcessingUnit.get(pu) == null
                || futureContainersPerProcessingUnit.get(pu) == null;
    }

    private void cleanFailedFutureContainers() {

        List<FutureGridServiceContainer> list = failedFutureContainers;
        final Iterator<FutureGridServiceContainer> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureGridServiceContainer future = iterator.next();
            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp().getTime()) / 1000);
            GridServiceAgent agent = future.getGridServiceAgent();
            if (!agent.isDiscovered()) {
                logger.info("Forgetting failure to start container on machine "
                        + ContainersSlaUtils.machineToString(agent.getMachine()) + " that occured "
                        + passedSeconds + " seconds ago since grid service agent no longer exists.");
                iterator.remove();
            } 
            else {
                terminateOrphanContainersOfAgent(agent);
                if (passedSeconds > START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS) {
                    logger.info("Forgetting failure to start container on machine "
                            + ContainersSlaUtils.machineToString(agent.getMachine()) + " that occured "
                            + passedSeconds + " seconds ago due to timeout.");
                    iterator.remove();
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
        for (final GridServiceContainer container : admin.getGridServiceContainers()) {
            if (container.getGridServiceAgent().equals(agent)) {
                agentIds.remove(container.getAgentId());
            }
        }
        
        for (final int agentId : agentIds) {
            try {
                agent.killByAgentId(agentId);
                logger.warn("Terminated orphan container that did not register with lookup service on machine " + ContainersSlaUtils.machineToString(agent.getMachine())+ " agentId=" + agentId);
            }
            catch (final AdminException e) {
                logger.warn("Error terminating orphan container that did not register with lookup service on machine " + ContainersSlaUtils.machineToString(agent.getMachine()) + " agentId=" + agentId, e);
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
