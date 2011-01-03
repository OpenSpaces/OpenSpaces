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
public class ContainersSlaEnforcement implements ServiceLevelAgreementEnforcement<ContainersSlaPolicy,ProcessingUnit,ContainersSlaEnforcementEndpoint> {

    private static final int START_CONTAINER_TIMEOUT_FAILURE_SECONDS = 60;
    private static final int START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS = 600;

    private static final Log logger = LogFactory.getLog(ContainersSlaEnforcement.class);
    
    private final Map<ProcessingUnit, List<GridServiceContainer>> containersMarkedForShutdownPerProcessingUnit;
    private final Map<ProcessingUnit, List<FutureGridServiceContainer>> futureContainersPerProcessingUnit;
    private final List<FutureGridServiceContainer> failedFutureContainers;

    private final InternalAdmin admin;
    private final Map<ProcessingUnit,ContainersSlaEnforcementEndpoint> endpoints;

    public ContainersSlaEnforcement(Admin admin) {
        this.admin = (InternalAdmin) admin;
        this.endpoints = new HashMap<ProcessingUnit, ContainersSlaEnforcementEndpoint>();
        this.containersMarkedForShutdownPerProcessingUnit = new HashMap<ProcessingUnit, List<GridServiceContainer>>();
        this.futureContainersPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureGridServiceContainer>>();
        this.failedFutureContainers = new ArrayList<FutureGridServiceContainer>();
    }

    /**
     * 
     * @param zone - the container zone
     * @return a service that continuously maintains the specified number of containers with the specified zone.
     */
    public ContainersSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
        throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        
        if (!isProcessingUnitDisposed(pu)) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu " + pu.getName() +" since an endpoint for the pu already exists.");
        }
        
        ProcessingUnit otherPu1 = getEndpointsWithSameNameAs(pu);
        if (otherPu1 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu " + pu.getName() +" since an endpoint for a pu with the same name already exists.");
        }
        
        ProcessingUnit otherPu2 = getEndpointsWithSameContainersZoneAs(pu);
        if (otherPu2 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu " + pu.getName() +" since an endpoint for a pu with the same (containers) zone already exists: " + otherPu2.getName() );
        }
        
        ContainersSlaEnforcementEndpoint endpoint = new ContainersSlaEnforcementEndpoint() {

            public GridServiceContainer[] getContainers() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                return ContainersSlaEnforcement.this.getContainers(pu);
            }

            public GridServiceContainer[] getContainersPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                return ContainersSlaEnforcement.this.getContainersPendingProcessingUnitRelocation(pu);
            }

            public boolean enforceSla(ContainersSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                return ContainersSlaEnforcement.this.enforceSla(pu, sla);
            }

            public ProcessingUnit getId() {
                return pu;
            }
        };
        
        
        
        endpoints.put(pu,endpoint);
        containersMarkedForShutdownPerProcessingUnit.put(pu, new ArrayList<GridServiceContainer>());
        futureContainersPerProcessingUnit.put(pu,new ArrayList<FutureGridServiceContainer>());
        
        return endpoint;
    }

    private ProcessingUnit getEndpointsWithSameContainersZoneAs(ProcessingUnit pu) {
        for (ProcessingUnit endpointPu : this.endpoints.keySet()) {
            if (getContainerZone(endpointPu).equals(getContainerZone(pu))) {
                return endpointPu;
            }
        }
        return null;
    }
    
    private ProcessingUnit getEndpointsWithSameNameAs(ProcessingUnit pu) {
        for (ProcessingUnit endpointPu : this.endpoints.keySet()) {
            if (endpointPu.getName().equals(pu.getName())) {
                return endpointPu;
            }
        }
        return null;
    }

    private String getContainerZone(ProcessingUnit pu) {
        String[] zones = pu.getRequiredZones();
        if (zones.length == 0) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. " + pu.getName() + " has been deployed with no zones defined.");
        }
        
        if (zones.length > 1) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. " + pu.getName() + " has been deployed with " + zones.length + " zones : "  + Arrays.toString(zones));
        }
        
        return zones[0];
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
    
    private GridServiceContainer[] getContainers(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDisposed(pu);   
        
        List<GridServiceContainer> containers = ContainersSlaUtils.getContainersByZone(getContainerZone(pu),admin);
        for (GridServiceContainer container : containersMarkedForShutdownPerProcessingUnit.get(pu)) {
            containers.remove(container);
        }
        return containers.toArray(new GridServiceContainer[containers.size()]);
    }

    
    private boolean enforceSla(ProcessingUnit pu, ContainersSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDisposed(pu);
        if (sla == null) {
           throw new IllegalArgumentException("sla cannot be null");
        }
        
        String[] zoneInContainerOptions = sla.getNewContainerConfig().getZones();
        
        String zone = getContainerZone(pu);
        if (zoneInContainerOptions.length != 1 || 
            !zoneInContainerOptions[0].equals(zone)) {
            throw new IllegalArgumentException("sla zone is " + Arrays.toString(zoneInContainerOptions) + " and instead it should be " + zone);
        }
       
       try {
            enforceSlaInternal(pu, sla);
        } catch (ConflictingOperationInProgressException e) {
            logger.info("Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",e);
            return false; // try again next time
        }
        
       return isSlaReached(pu,sla);
    }

    private GridServiceContainer[] getContainersPendingProcessingUnitRelocation(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDisposed(pu);
        List<GridServiceContainer> containers = containersMarkedForShutdownPerProcessingUnit.get(pu);
        return containers.toArray(new GridServiceContainer[containers.size()]);
    }

    
    private void enforceSlaInternal(ProcessingUnit pu, final ContainersSlaPolicy sla) throws ConflictingOperationInProgressException {
        
        cleanContainersMarkedForShutdown(pu);
        cleanFutureContainers(pu);
        
        String zone = getContainerZone(pu);
        List<GridServiceContainer> containers = ContainersSlaUtils.getContainersByZone(zone,admin);
        
        int targetContainers = Math.max(sla.getTargetNumberOfContainers(), sla.getMinimumNumberOfMachines());
        int existingContainers = containers.size();
        int futureContainers = this.futureContainersPerProcessingUnit.get(pu).size();
        
        if (existingContainers > targetContainers) {
            // scale in
            int containersSurplus = existingContainers - targetContainers;
            List<GridServiceContainer> containersMarkedForShutdown = this.containersMarkedForShutdownPerProcessingUnit.get(pu); 
            // remove containers mark for shutdown if there are too many of them 
            while (containersMarkedForShutdown.size() > containersSurplus) {
                containersMarkedForShutdown.remove(0);
            }
            
            // add containers mark for shutdown if there are too few of them
            for (int i = 0 ; 
                 containersMarkedForShutdown.size()< containersSurplus && i < existingContainers ; 
                 i++) {
                
                GridServiceContainer container = containers.get(i);
                if (!containersMarkedForShutdown.contains(container)) {
                    containersMarkedForShutdown.add(container);
                }
            }
            
            // assert we marked just enough containers for shutdown
            if (containersMarkedForShutdown.size() != containersSurplus) {
                throw new IllegalStateException("Could not find enough containers to shutdown in order to reach target of " + targetContainers + " containers.");
            }
        }
        
        else if (existingContainers < targetContainers) {
         // scale out
            int requiredContainers = targetContainers - (existingContainers + futureContainers);
            int missingCapacityInMB = 0;
            for (int i = 0 ; i < requiredContainers ; i++) {
                
                try {
                    final GridServiceAgent gsa = findGridServiceAgentForNewContainer(pu, sla);
                                        
                    this.futureContainersPerProcessingUnit.get(pu).add(
                       ContainersSlaUtils.startGridServiceContainerAsync(
                               admin, 
                               (InternalGridServiceAgent)gsa,
                               sla.getNewContainerConfig(),
                               START_CONTAINER_TIMEOUT_FAILURE_SECONDS,TimeUnit.SECONDS));
                } catch (NeedMoreMachinesException e) {
                    missingCapacityInMB += e.getMissingCapacityInMB();
                }
            }
        }
    }

    private GridServiceAgent findGridServiceAgentForNewContainer(ProcessingUnit pu, ContainersSlaPolicy sla) throws NeedMoreMachinesException, ConflictingOperationInProgressException {
        
        long requiredFreeMemoryInMB = sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB();
        
        boolean conflictingOperationInProgress = false;
        
        List<GridServiceAgent> recommendedAgents = new ArrayList<GridServiceAgent>();
        
        for (GridServiceAgent gsa : sla.getGridServiceAgents()) {
            Machine machine = gsa.getMachine();
            
            if (isFutureGridServiceContainerOnMachine(machine)) {
                // try another machine, we check the flag if all else fails.
                conflictingOperationInProgress = true;
                continue;
            }
            
            if (sla.getMaximumNumberOfContainersPerMachine() != 0 &&
                ContainersSlaUtils.getContainersByZoneOnMachine(getContainerZone(pu),machine).size() >= 
                    sla.getMaximumNumberOfContainersPerMachine()) {
                // hit maximum number of containers per machine sla limit
                continue;
            }
            
            final OperatingSystemStatistics operatingSystemStatistics = machine.getOperatingSystem().getStatistics();
            
            // get total free system memory + cached (getActualFreePhysicalMemorySizeInMB returns -1
            // when not using Sigar)
            int totalFreePhysicalMemorySizeInMB = (int) Math.floor(
                    (operatingSystemStatistics.getActualFreePhysicalMemorySizeInBytes() > -1 ? 
                            operatingSystemStatistics.getActualFreePhysicalMemorySizeInMB() : 
                                operatingSystemStatistics.getFreePhysicalMemorySizeInMB()));
            
            if (totalFreePhysicalMemorySizeInMB > requiredFreeMemoryInMB + sla.getReservedPhysicalMemoryPerMachineInMB()) {
                recommendedAgents.add(gsa);
            }
        }
        
        Set<Machine> machines =new HashSet<Machine>();
        for (GridServiceContainer container : getContainers(pu)) {
            machines.add(container.getMachine());
        }
        if (recommendedAgents.size() > 0) {
            
            if (machines.size() >= sla.getMinimumNumberOfMachines()) {
                // minimum number of machines have been reached. just pick and recommended agent. 
                return recommendedAgents.get(0);
            }
            
            for (GridServiceAgent recommendedAgent : recommendedAgents) {
                if (!machines.contains(recommendedAgent.getMachine())) {
                 // this is a new machine and we need more machines to meet the minimum number of machines.
                    return recommendedAgent;
                }
            }
            
        }
        
        if (conflictingOperationInProgress) {
            throw new ConflictingOperationInProgressException();
        }
        
        throw new NeedMoreMachinesException(requiredFreeMemoryInMB);
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
    

    
    /**
     * @return true if reached exact target number of containers with specified container zone.
     */
    private boolean isSlaReached(ProcessingUnit pu, ContainersSlaPolicy sla) {
        return 
            sla.getTargetNumberOfContainers() == ContainersSlaUtils.getContainersByZone(getContainerZone(pu),admin).size() &&
            containersMarkedForShutdownPerProcessingUnit.get(pu).size() == 0 &&
            futureContainersPerProcessingUnit.get(pu).size() == 0;
    }

    private void validateNotDisposed(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
 
        if (isProcessingUnitDisposed(pu)) {
            
            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }
    
    private boolean isProcessingUnitDisposed(ProcessingUnit pu) {
        
        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        return  !endpoints.containsKey(pu) ||
                containersMarkedForShutdownPerProcessingUnit.get(pu) == null || 
                futureContainersPerProcessingUnit.get(pu) == null;
    }
    
    private void cleanContainersMarkedForShutdown(ProcessingUnit pu) {

        final List<GridServiceContainer> containersPendingRelocation = containersMarkedForShutdownPerProcessingUnit.get(pu);
        final Iterator<GridServiceContainer> iterator = containersPendingRelocation.iterator();
        while (iterator.hasNext()) {
            final GridServiceContainer container = iterator.next();
            
            if (!container.isRunning()) {
                iterator.remove();
            }
            
            else if (container.getProcessingUnitInstances().length == 0) {
                admin.scheduleAdminOperation(new Runnable() {
                    
                    public void run() {
                        container.kill();
                    }
                });
            }
        }
    }
    
    private void cleanFutureContainers(ProcessingUnit pu) {
        List<FutureGridServiceContainer> list = futureContainersPerProcessingUnit.get(pu);
        final Iterator<FutureGridServiceContainer> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureGridServiceContainer future = iterator.next();
            
            if (future.isDone()) {
            
                iterator.remove();
                
                Exception exception = null;
                
                try {
                    
                    if (future.getGridServiceAgent().isRunning()) {
                        GridServiceContainer container = future.get();
                        logger.info("Container started succesfully " + ToStringHelper.gscToString(container));
                    }
                    
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof AdminException) {
                        exception = (AdminException) e.getCause();
                    }
                    else {
                        throw new IllegalStateException("Unexpected runtime exception",e);
                    }
                } catch (TimeoutException e) {
                    exception = e;
                }
                
                if (exception != null) {
                    final String errorMessage = "Failed to start container on machine " + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine());
                    logger.warn(errorMessage , exception);
                    this.failedFutureContainers.add(future);
                }
            }
        }
        
        cleanFailedFutureContainers();

    }
        
    private void cleanFailedFutureContainers() {

        List<FutureGridServiceContainer> list = failedFutureContainers;
        final Iterator<FutureGridServiceContainer> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureGridServiceContainer future = iterator.next();
            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp().getTime()) / 1000);
            if (!future.getGridServiceAgent().isDiscovered()) {
                logger.info("Forgetting failure to start container on machine " + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine()) + " that occured " + passedSeconds + " seconds ago since grid service agent no longer exists.");
                iterator.remove();
            }
            else if ( passedSeconds > START_CONTAINER_TIMEOUT_FAILURE_FORGET_SECONDS) {
                logger.info("Forgetting failure to start container on machine " + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine()) + " that occured " + passedSeconds + " seconds ago due to timeout.");
                iterator.remove();
            }
        }
    }

    private static class ConflictingOperationInProgressException extends Exception {}
    
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
