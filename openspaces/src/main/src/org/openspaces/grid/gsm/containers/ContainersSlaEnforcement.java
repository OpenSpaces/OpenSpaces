package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.openspaces.grid.esm.ToStringHelper;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

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

    private static final int START_CONTAINER_TIMEOUT_FAILURE_SECONDS = 60;
    private static final int START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS = 60;

    private static final Log logger = LogFactory.getLog(ContainersSlaEnforcement.class);

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

        private ProcessingUnit pu;

        public DefaultContainersSlaEnforcementEndpoint(ProcessingUnit pu) {
            this.pu = pu;
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
                ContainersSlaEnforcement.logger.info(
                        "Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",
                        e);
                return false; // try again next time
            } catch (NeedMoreMachinesException e) {
                ContainersSlaEnforcement.logger.warn(
                        "Cannot enforce Containers SLA since there are not enough machines available. Need more "
                                + e.getMissingCapacityInMB() + "MB RAM", e);
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
                NeedMoreMachinesException {

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
                        containersMarkedForShutdown.add(containerToRemove);
                        approvedContainers.remove(containerToRemove);
                    }
                }
            } else {
                // try to scale out until SLA is met
                while (!ContainersSlaUtils.isFutureCapacityMet(sla, approvedContainers, futureContainers)) {

                    // bring back a container that is marked for shutdown to the approved containers
                    // list
                    GridServiceContainer unmarkContainer = null;

                    for (GridServiceContainer containerMarkedForShutdown : containersMarkedForShutdown) {
                        // unmark only containers that are managed by an approved agent
                        if (approvedAgents.contains(containerMarkedForShutdown.getGridServiceAgent())) {
                            unmarkContainer = containerMarkedForShutdown;
                            break;
                        }
                    }
                    if (unmarkContainer != null) {
                        containersMarkedForShutdownPerProcessingUnit.remove(unmarkContainer);
                        approvedContainers.add(unmarkContainer);
                        continue;
                    }

                    // deploy a new container on an approved machine that has the least number of
                    // containers.
                    final GridServiceAgent gsa = findAgentForNewContainer(pu, sla);
                    logger.info("Starting a new Grid Service Container on " + ToStringHelper.machineToString(gsa.getMachine()));
                    futureContainers.add(ContainersSlaUtils.startGridServiceContainerAsync(admin,
                            (InternalGridServiceAgent) gsa, sla.getNewContainerConfig(),
                            START_CONTAINER_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS));

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
                            "Cannot remove container " + ToStringHelper.gscToString(container) + " since " +
                            (ContainersSlaUtils.getMemoryCapacityShortageInMB(sla, containers) <= 0 ? " it would violate memory SLA." :
                             ContainersSlaUtils.getCpuCapacityShortage(sla, containers) <= 0 ? " it would violate CPU sla." :
                             ContainersSlaUtils.getMachineShortage(sla, containers) <= 0 ? " it would violate minimum number of machines." : "unknown"));
                    }
                }
            }

            return null;
        }

        private GridServiceAgent findAgentForNewContainer(ProcessingUnit pu, ContainersSlaPolicy sla)
                throws NeedMoreMachinesException, ConflictingOperationInProgressException {

            List<GridServiceAgent> recommendedAgents = findAgentsForNewContainerSortByNumberOfContainersInZone(pu, sla);

            // Pick the most recommended agent.
            return recommendedAgents.get(0);

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
                            logger.info("Container started succesfully " + ToStringHelper.gscToString(container));
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
                                + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine());
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
     * finds all Grid Service Agents that have enough free space for a new grid service container.
     * The resultint agents are sorted by the number of containers (with the same zone) per machine.
     * @param pu
     * @param sla
     * @return
     * @throws ConflictingOperationInProgressException
     * @throws NeedMoreMachinesException
     */
    private List<GridServiceAgent> findAgentsForNewContainerSortByNumberOfContainersInZone(ProcessingUnit pu,
            ContainersSlaPolicy sla) throws ConflictingOperationInProgressException, NeedMoreMachinesException {

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
                logger.debug(ToStringHelper.machineToString(gsa.getMachine()) + " does not have enough free memory. It has only " + freeInMB + "MB free and required is " + requiredFreeMemoryInMB + "MB plus reserved is " + sla.getReservedMemoryCapacityPerMachineInMB()+"MB");
            }
        }

        if (recommendedAgents.size() == 0) {
            if (conflictingOperationInProgress) {
                throw new ConflictingOperationInProgressException();
            }
            throw new NeedMoreMachinesException(requiredFreeMemoryInMB);
        }

        return recommendedAgents;
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
            if (!future.getGridServiceAgent().isDiscovered()) {
                logger.info("Forgetting failure to start container on machine "
                        + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine()) + " that occured "
                        + passedSeconds + " seconds ago since grid service agent no longer exists.");
                iterator.remove();
            } else if (passedSeconds > START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS) {
                logger.info("Forgetting failure to start container on machine "
                        + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine()) + " that occured "
                        + passedSeconds + " seconds ago due to timeout.");
                iterator.remove();
            }
        }
    }

    @SuppressWarnings("serial")
    private static class ConflictingOperationInProgressException extends Exception {
    }

    @SuppressWarnings("serial")
    private static class NeedMoreMachinesException extends Exception {

        private final long missingCapacityInMB;

        NeedMoreMachinesException(long missingCapacityInMB) {
            this.missingCapacityInMB = missingCapacityInMB;
        }

        public long getMissingCapacityInMB() {
            return missingCapacityInMB;
        }

    }

}
