package org.openspaces.grid.gsm.containers;

import java.util.ArrayList;
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
public class ContainersSlaEnforcement implements ServiceLevelAgreementEnforcement<ContainersSlaPolicy,String,ContainersSlaEnforcementEndpoint> {

    private static final int START_CONTAINER_TIMEOUT_FAILURE_SECONDS = 60;
    private static final int START_CONTAINER_TIMEOUT_FAILURE_IGNORE_SECONDS = 600;

    private static final Log logger = LogFactory.getLog(ContainersSlaEnforcement.class);
    
    private final Map<String,List<GridServiceContainer>> containersMarkedForShutdownPerZone;
    private final Map<String,List<FutureGridServiceContainer>> futureContainersPerZone;
    private final List<FutureGridServiceContainer> failedFutureContainers;

    private final InternalAdmin admin;
    private final Map<String,ContainersSlaEnforcementEndpoint> services;

    public ContainersSlaEnforcement(Admin admin) {
        this.admin = (InternalAdmin) admin;
        this.services = new HashMap<String,ContainersSlaEnforcementEndpoint>();
        this.containersMarkedForShutdownPerZone = new HashMap<String, List<GridServiceContainer>>();
        this.futureContainersPerZone = new HashMap<String, List<FutureGridServiceContainer>>();
        this.failedFutureContainers = new ArrayList<FutureGridServiceContainer>();
    }

    /**
     * 
     * @param zone - the container zone
     * @return a service that continuously maintains the specified number of containers with the specified zone.
     */
    public ContainersSlaEnforcementEndpoint createEndpoint(final String zone)
        throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        
        if (!isZoneDisposed(zone)) {
            throw new IllegalStateException("Cannot initialize a new ContainersAdminService for zone " + zone +" since it already exists." );
        }
        
        ContainersSlaEnforcementEndpoint service = new ContainersSlaEnforcementEndpoint() {

            public GridServiceContainer[] getContainers() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                return ContainersSlaEnforcement.this.getContainers(zone);
            }

            public GridServiceContainer[] getContainersPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                return ContainersSlaEnforcement.this.getContainersPendingProcessingUnitRelocation(zone);
            }

            public boolean enforceSla(ContainersSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                return ContainersSlaEnforcement.this.enforceSla(zone,sla);
            }

            public String getId() {
                return zone;
            }
        };
        
        
        
        services.put(zone,service);
        containersMarkedForShutdownPerZone.put(zone, new ArrayList<GridServiceContainer>());
        futureContainersPerZone.put(zone,new ArrayList<FutureGridServiceContainer>());
        
        return service;
    }

    public void destroyEndpoint(String zone) {
        containersMarkedForShutdownPerZone.remove(zone);
        futureContainersPerZone.remove(zone);
        services.remove(zone);
    }

    public void destroy() {
        for (String zone : services.keySet()) {
            destroyEndpoint(zone);
        }
    }
    
    private GridServiceContainer[] getContainers(String zone) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDisposed(zone);   
        
        List<GridServiceContainer> containers = ContainersSlaUtils.getContainersByZone(zone,admin);
        for (GridServiceContainer container : containersMarkedForShutdownPerZone.get(zone)) {
            containers.remove(container);
        }
        return containers.toArray(new GridServiceContainer[]{});
    }

    
    private boolean enforceSla(String zone, ContainersSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDisposed(zone);
        if (sla == null) {
           throw new IllegalArgumentException("sla cannot be null");
        }
        
        String[] zoneInContainerOptions = ContainersSlaUtils.getZones(sla.getNewContainerOptions());
        
        if (zoneInContainerOptions.length != 1 || !zoneInContainerOptions[0].equals(zone)) {
            throw new IllegalArgumentException("grid container options zone is " + zoneInContainerOptions + " instead of " + zone);
        }
       
       try {
            run(zone,sla);
        } catch (ConflictingOperationInProgressException e) {
            logger.info("Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",e);
            return false; // try again next time
        }
        
       return isSlaReached(zone,sla);
    }

    private GridServiceContainer[] getContainersPendingProcessingUnitRelocation(String zone) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDisposed(zone);
        return containersMarkedForShutdownPerZone.get(zone).toArray(new GridServiceContainer[]{});
    }

    
    private void run(String zone, final ContainersSlaPolicy sla) throws ConflictingOperationInProgressException {
        
        cleanContainersMarkedForShutdown(zone);
        cleanFutureContainers(zone);
        
        List<GridServiceContainer> containers = ContainersSlaUtils.getContainersByZone(zone,admin);
        
        int targetContainers = sla.getTargetNumberOfContainers();
        int existingContainers = containers.size();
        int futureContainers = this.futureContainersPerZone.get(zone).size();
        
        if (existingContainers > targetContainers) {
            // scale in
            int containersSurplus = existingContainers - targetContainers;
            List<GridServiceContainer> containersMarkedForShutdown = this.containersMarkedForShutdownPerZone.get(zone); 
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
                    final GridServiceAgent gsa = findGridServiceAgentForNewContainer(zone, sla);
                                        
                    this.futureContainersPerZone.get(zone).add(
                       ContainersSlaUtils.startGridServiceContainerAsync(
                               admin, 
                               (InternalGridServiceAgent)gsa,
                               sla.getNewContainerOptions(),
                               START_CONTAINER_TIMEOUT_FAILURE_SECONDS,TimeUnit.SECONDS));
                } catch (NeedMoreMachinesException e) {
                    missingCapacityInMB += e.getMissingCapacityInMB();
                }
            }
        }
    }

    private GridServiceAgent findGridServiceAgentForNewContainer(String zone, ContainersSlaPolicy sla) throws NeedMoreMachinesException, ConflictingOperationInProgressException {
        
        //TODO: Take into account maximum number of containers per machine
        int requiredFreeMemoryInMB = ContainersSlaUtils.getMaximumJavaHeapSizeInMB(sla.getNewContainerOptions());
        
        boolean conflictingOperationInProgress = false;
        GridServiceAgent recommendedGsa = null;
        
        for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
            Machine machine = gsa.getMachine();
            
            if (isFutureGridServiceContainerOnMachine(machine)) {
                // try another machine, we check the flag if all else fails.
                conflictingOperationInProgress = true;
                continue;
            }
            
            if (sla.getMaximumNumberOfContainersPerMachine() != 0 &&
                ContainersSlaUtils.getContainersByZoneOnMachine(zone,machine).size() >= sla.getMaximumNumberOfContainersPerMachine()) {
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
                recommendedGsa = gsa;
                break;
            }
        }
        
        if (recommendedGsa != null) {
            return recommendedGsa;
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
        
        for (List<FutureGridServiceContainer> futures : this.futureContainersPerZone.values()) {
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
    private boolean isSlaReached(String zone, ContainersSlaPolicy sla) {
        return 
            sla.getTargetNumberOfContainers() == ContainersSlaUtils.getContainersByZone(zone,admin).size() &&
            containersMarkedForShutdownPerZone.get(zone).size() == 0 &&
            futureContainersPerZone.get(zone).size() == 0;
    }

    private void validateNotDisposed(String zone) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
 
        if (isZoneDisposed(zone)) {
            
            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }
    
    private boolean isZoneDisposed(String zone) {
        
        if (zone == null) {
            throw new IllegalArgumentException("zone cannot be null");
        }
        
        return  !services.containsKey(zone) ||
                containersMarkedForShutdownPerZone.get(zone) == null || 
                futureContainersPerZone.get(zone) == null;
    }
    
    private void cleanContainersMarkedForShutdown(String zone) {

        final List<GridServiceContainer> containersPendingRelocation = containersMarkedForShutdownPerZone.get(zone);
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
    
    private void cleanFutureContainers(String zone) {
        List<FutureGridServiceContainer> list = futureContainersPerZone.get(zone);
        final Iterator<FutureGridServiceContainer> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureGridServiceContainer future = iterator.next();
            
            if (future.isDone()) {
            
                iterator.remove();
                
                Exception exception = null;
                
                try {
                    
                    if (future.getGridServiceAgent().isRunning()) {
                        GridServiceContainer container = future.getGridServiceContainer();
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
            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp()) / 1000);
            if (!future.getGridServiceAgent().isRunning()) {
                logger.info("Ignoring failure to start container on machine " + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine()) + " that occured " + passedSeconds + " seconds ago since grid service agent no longer exists.");
                iterator.remove();
            }
            else if ( passedSeconds > START_CONTAINER_TIMEOUT_FAILURE_IGNORE_SECONDS) {
                logger.info("Ignoring failure to start container on machine " + ToStringHelper.machineToString(future.getGridServiceAgent().getMachine()) + " that occured " + passedSeconds + " seconds ago due to timeout.");
                iterator.remove();
            }
        }
    }

    class ConflictingOperationInProgressException extends Exception {}
    class NeedMoreMachinesException extends Exception {
        private final int missingCapacityInMB;

        NeedMoreMachinesException(int missingCapacityInMB) {
            this.missingCapacityInMB = missingCapacityInMB;
        }

        public int getMissingCapacityInMB() {
            return missingCapacityInMB;
        }
        
    }
}
