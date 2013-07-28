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
package org.openspaces.grid.gsm.rebalancing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.rebalancing.exceptions.RemovedContainerProcessingUnitDeploymentException;
import org.openspaces.grid.gsm.rebalancing.exceptions.SpaceRecoveryAfterRelocationException;
import org.openspaces.grid.gsm.rebalancing.exceptions.WrongContainerProcessingUnitRelocationException;

import com.gigaspaces.cluster.activeelection.SpaceMode;

public class RebalancingUtils {

   static Collection<FutureStatelessProcessingUnitInstance> incrementNumberOfStatelessInstancesAsync(
       final ProcessingUnit pu, 
       final GridServiceContainer[] containers,
       final Log logger,
       final long duration, final TimeUnit timeUnit) {
       
       if (pu.getMaxInstancesPerVM() != 1) {
           throw new IllegalArgumentException("Only one instance per VM is allowed");
       }
       
       List<GridServiceContainer> unusedContainers = getUnusedContainers(pu,containers);
       
       final Admin admin = pu.getAdmin();
       final Map<GridServiceContainer,FutureStatelessProcessingUnitInstance> futureInstances = new HashMap<GridServiceContainer, FutureStatelessProcessingUnitInstance>();
       
       final AtomicInteger targetNumberOfInstances = new AtomicInteger(pu.getNumberOfInstances());
       
       final long start = System.currentTimeMillis();
       final long end = start + timeUnit.toMillis(duration);
       
       for (GridServiceContainer container : unusedContainers) {
           final GridServiceContainer targetContainer = container;
           futureInstances.put(container, new FutureStatelessProcessingUnitInstance() {

               AtomicReference<Throwable> throwable = new AtomicReference<Throwable>();
               ProcessingUnitInstance newInstance;
               
               public boolean isTimedOut() {
                   return System.currentTimeMillis() > end;
               }

               public boolean isDone() {
                   
                   end();
                   
                   return isTimedOut() ||
                          throwable.get() != null || 
                          newInstance != null;
               }
               
               public ProcessingUnitInstance get() throws ExecutionException, IllegalStateException, TimeoutException {

                   end();
                   
                   if (getException() != null) {
                       throw getException();
                   }
                   
                   if (newInstance == null) {
                       if (isTimedOut()) {
                           throw new TimeoutException("Relocation timeout");
                       }
                       throw new IllegalStateException("Async operation is not done yet.");
                   }
                   
                   return newInstance;
               }
               
               public Date getTimestamp() {
                   return new Date(start);
               }

               public ExecutionException getException() {

                   end();
                   Throwable t = throwable.get();
                   if (t != null) {
                       return new ExecutionException(t.getMessage(), t);
                   }
                   return null;
               }

            
            public GridServiceContainer getTargetContainer() {
                return targetContainer;
            }
                        
            public ProcessingUnit getProcessingUnit() {
                return pu;
            }
            
            public String getFailureMessage() throws IllegalStateException {
                if (isTimedOut()) {
                    return "deployment timeout of processing unit " + pu.getName() + " on " + 
                            gscToString(targetContainer);
                   }
                   
                   if (getException() != null) {
                       return getException().getMessage();
                   }
                   
                   throw new IllegalStateException("Relocation has not encountered any failure.");
            }
            
            private void end() {
                
                if (!targetContainer.isDiscovered()) {
                    throwable.set(
                                    new RemovedContainerProcessingUnitDeploymentException(
                                            pu,targetContainer));
                }
                
                else if (throwable.get() != null || newInstance != null) {
                    //do nothing. idempotent method
                }
                
                else {
                    incrementInstance();
                    
                    ProcessingUnitInstance[] instances = targetContainer.getProcessingUnitInstances(pu.getName());
                
                    if (instances.length > 0) {
                        newInstance = instances[0];
                    }
                }
            }
            
            private void incrementInstance() {
                final String uuid = "[incrementUid:"+UUID.randomUUID().toString()+"] ";
                int numberOfInstances = pu.getNumberOfInstances();
                int maxNumberOfInstances = getContainersOnMachines(pu).length;
                if (numberOfInstances < maxNumberOfInstances) {
                   if (targetNumberOfInstances.get() == numberOfInstances+1) {
                       if (logger.isInfoEnabled()) {
                           logger.info(uuid+
                                  " Waiting for pu.numberOfInstances to increment from "+numberOfInstances + " to " + targetNumberOfInstances.get() + ". "+
                                  "Number of relevant containers " + maxNumberOfInstances);
                       }
                   }
                   else if (admin.getGridServiceManagers().getSize() > 1 && !((InternalProcessingUnit)pu).isBackupGsmInSync() ) {
                	   if (logger.isInfoEnabled()) {
                           logger.info(uuid+" Waiting for backup gsm to sync with active gsm");
                       }
                   }
                   else {
                       targetNumberOfInstances.set(numberOfInstances+1);
                       if (logger.isInfoEnabled()) {
                           logger.info(uuid+
                                  "Planning to increment pu.numberOfInstances from "+numberOfInstances + " to " + targetNumberOfInstances.get() + ". "+
                                  "Number of relevant containers " + maxNumberOfInstances);
                       }
                       ((InternalAdmin) admin).scheduleAdminOperation(new Runnable() {
                           public void run() {
                               try {
                                   // this is an async operation 
                                   // pu.getNumberOfInstances() still shows the old value.
                                   pu.incrementInstance();
                                   if (logger.isInfoEnabled()) {
                                       logger.info(uuid+"pu.incrementInstance() called");
                                   }
                               } catch (AdminException e) {
                                   throwable.set(e);
                               } catch (Throwable e) {
                                   logger.error(uuid+"Unexpected Exception: " + e.getMessage(),e);
                                   throwable.set(e);
                               }
                           }
                       });
                   }
               }
            }
        });
            
       }
       
       return futureInstances.values();
       
   }

   private static List<GridServiceContainer> getUnusedContainers(final ProcessingUnit pu, final GridServiceContainer[] containers) {
       // look for free containers
       List<GridServiceContainer> unusedContainers = new ArrayList<GridServiceContainer>();
       for (GridServiceContainer container : containers) {
           if (container.getProcessingUnitInstances(pu.getName()).length == 0) {
               unusedContainers.add(container);
           }
       }
       return unusedContainers;
   }
   
   static FutureStatefulProcessingUnitInstance relocateProcessingUnitInstanceAsync(
           final GridServiceContainer targetContainer,
           final ProcessingUnitInstance puInstance, 
           final Log logger,
           final long duration, final TimeUnit timeUnit) {

        final ProcessingUnit pu = puInstance.getProcessingUnit();
        final GridServiceContainer[] replicationSourceContainers = getReplicationSourceContainers(puInstance); 
        final int instanceId = puInstance.getInstanceId();
        
        final CountDownLatch relocateInProgress = new CountDownLatch(1);
        final AtomicReference<Throwable> relocateThrowable = new AtomicReference<Throwable>();

        final Admin admin = puInstance.getAdmin();
        final int runningNumber = puInstance.getClusterInfo().getRunningNumber();
        final String puName = puInstance.getName();
        
        final GridServiceContainer sourceContainer = puInstance.getGridServiceContainer();
        final Set<ProcessingUnitInstance> puInstancesFromSamePartition = 
            getOtherInstancesFromSamePartition(puInstance);
        
        if (puInstancesFromSamePartition.size() != pu.getNumberOfBackups()) {
            throw new IllegalStateException(
                    "puInstancesFromSamePartition has " + puInstancesFromSamePartition.size() + 
                    " instances instead of " + pu.getNumberOfBackups());
        }

        final long start = System.currentTimeMillis();
        final long end = start + timeUnit.toMillis(duration);
        
        ((InternalAdmin) admin).scheduleAdminOperation(new Runnable() {
            public void run() {
                try {
                    puInstance.relocate(targetContainer);
                }
                catch (AdminException e) {
                    relocateThrowable.set(e);
                } catch (Throwable e) {
                    logger.error("Unexpected exception " + e.getMessage(),e);
                    relocateThrowable.set(e);
                }
                finally {
                    relocateInProgress.countDown();
                }
            }
        });

        FutureStatefulProcessingUnitInstance future = new FutureStatefulProcessingUnitInstance() {

            Throwable throwable;
            ProcessingUnitInstance newInstance;
            
            public boolean isTimedOut() {
                return System.currentTimeMillis() > end;
            }

            public boolean isDone() {
                
                endRelocation();
                
                return isTimedOut() ||
                       throwable != null || 
                       newInstance != null;
            }
            
            public ProcessingUnitInstance get() throws ExecutionException, IllegalStateException, TimeoutException {

                endRelocation();
                
                if (getException() != null) {
                    throw getException();
                }
                if (newInstance == null) {
                    if (isTimedOut()) {
                        throw new TimeoutException("Relocation timeout");
                    }
                    throw new IllegalStateException("Async operation is not done yet.");
                }
                
                return newInstance;
            }
            
            public Date getTimestamp() {
                return new Date(start);
            }

            public ExecutionException getException() {

                endRelocation();
                if (throwable != null) {
                    return new ExecutionException(throwable.getMessage(), throwable);
                }
                return null;
            }

            /**
             * populates this.exception or this.newInstance if relocation is complete
             */
            private void endRelocation() {
                boolean inProgress = true;
                try {
                    inProgress = !relocateInProgress.await(0L,TimeUnit.SECONDS);
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                }
                
                if (inProgress) {
                    // do nothing. relocate() method running on another thread has not returned yet.
                }
                else if (throwable != null || newInstance != null) {
                    //do nothing. idempotent method
                }
                else if (relocateThrowable.get() != null) {
                    throwable = relocateThrowable.get();
                }
                
                else if (!targetContainer.isDiscovered()) {
                    throwable = new RemovedContainerProcessingUnitDeploymentException(puInstance,targetContainer);
                } 
                else if (pu.getNumberOfBackups() > 0 && !isAtLeastOneInstanceValid(puInstancesFromSamePartition)) {
                        throwable = new SpaceRecoveryAfterRelocationException(puInstance, puInstancesFromSamePartition);
                } 
                else {
                    
                    ProcessingUnitInstance relocatedInstance = getRelocatedProcessingUnitInstance();
                    if (relocatedInstance != null) {
                        
                        if (relocatedInstance.getGridServiceContainer().equals(targetContainer)) {
                            if (relocatedInstance.getSpaceInstance() != null &&
                                relocatedInstance.getSpaceInstance().getMode() != SpaceMode.NONE) {
                                newInstance = relocatedInstance;
                            }
                        }
                        else  { 
                            throwable = new WrongContainerProcessingUnitRelocationException(puInstance,targetContainer);
                                         
                        }
                    }
                    else {
                        // do nothing. No state change means operation is not done yet.
                    }
                }
            }

            private ProcessingUnitInstance getRelocatedProcessingUnitInstance() {
                for (GridServiceContainer container : admin.getGridServiceContainers()) {
                    for (ProcessingUnitInstance instance : container.getProcessingUnitInstances(puName)) {
                        if (!instance.equals(puInstance) &&
                            instance.getClusterInfo().getRunningNumber() == runningNumber &&
                            !puInstancesFromSamePartition.contains(instance)) {
                            return instance;
                        }
                    }
                }
                return null;
            }

            private boolean isAtLeastOneInstanceValid(Set<ProcessingUnitInstance> instances) {
                boolean isValidState = false;
                for (ProcessingUnitInstance instance : instances) {
                    if (instance.isDiscovered() && instance.getGridServiceContainer().isDiscovered()) {
                        isValidState = true;
                        break;
                    }
                }
                return isValidState;
            }

           
            public String getFailureMessage() {
               if (isTimedOut()) {
                return "relocation timeout of processing unit instance " + instanceId + " from " + 
                        gscToString(sourceContainer) + " to " + 
                        gscToString(targetContainer);
               }
               
               if (getException() != null) {
                   return getException().getMessage();
               }
               
               throw new IllegalStateException("Relocation has not encountered any failure.");
               
            }

            public GridServiceContainer getTargetContainer() {
                return targetContainer;
            }

            public ProcessingUnit getProcessingUnit() {
                return pu;
            }

            public int getInstanceId() {
                return instanceId;
            }

            public GridServiceContainer getSourceContainer() {
                return sourceContainer;
            }

            public GridServiceContainer[] getReplicaitonSourceContainers() {
                return replicationSourceContainers;
            }

        };

        return future;
    }

   /**
    * @param instance
    * @return list of containers that are used by the relocated processing unit instance to synchronize all data.
    */
    public static GridServiceContainer[] getReplicationSourceContainers(ProcessingUnitInstance instance) {
        Set<GridServiceContainer> repContainers = new HashSet<GridServiceContainer>();
        
        GridServiceContainer[] containers = instance.getAdmin().getGridServiceContainers().getContainers();
        
        int numberOfBackups = instance.getProcessingUnit().getNumberOfBackups();
        if (numberOfBackups == 0) {
            return new GridServiceContainer[] {};
        }
        
        if (!isProcessingUnitPartitionIntact(instance.getProcessingUnit(), instance.getInstanceId(), containers)) {
            throw new IllegalStateException("Cannot relocate pu instance " + puInstanceToString(instance) +" since partition is not intact." );
        }
        
        for (int backupId = 0 ; backupId <= numberOfBackups ; backupId++) {
            if (backupId != instance.getBackupId()) {
                repContainers.add(findProcessingUnitInstance(instance.getProcessingUnit(),instance.getInstanceId(), backupId, containers).getGridServiceContainer());
            }
        }
        
        return repContainers.toArray(new GridServiceContainer[repContainers.size()]);
    }
    
    public static boolean isProcessingUnitIntact(ProcessingUnit pu, GridServiceContainer[] containers) {
        boolean intact = true;
        if (pu.getStatus() != DeploymentStatus.INTACT) {
            intact = false;
        }
        else {
            if (pu.getNumberOfBackups() > 0) {
                for (int instanceId = 1 ; intact && instanceId <= pu.getNumberOfInstances() ; instanceId ++) {
                    if (!isProcessingUnitPartitionIntact(pu, instanceId, containers)) {
                        intact = false;
                        break;
                    }
                }
            }
            else {
                ProcessingUnitInstance[] instances = pu.getInstances();
                if (instances.length < pu.getNumberOfInstances()) {
                    intact = false;
                }
                else {
                    for (ProcessingUnitInstance instance : instances) {
                        if (findProcessingUnitInstance(pu, instance.getInstanceId(), 0, containers) == null) {
                            intact = false;
                            break;
                        }
                    }
                }
            }
        }
        
        return intact;
    }
    
    public static boolean isProcessingUnitHasMinimumNumberOfInstancesPerPartition(ProcessingUnit pu, int minimumNumberOfInstancesPerPartition) {
        return isProcessingUnitHasMinimumNumberOfInstancesPerPartition(pu, pu.getAdmin().getGridServiceContainers().getContainers(), minimumNumberOfInstancesPerPartition);
    }
    
    private static boolean isProcessingUnitHasMinimumNumberOfInstancesPerPartition(ProcessingUnit pu, GridServiceContainer[] containers, int minimumNumberOfInstancesPerPartition) {
                
        boolean hasMinimum = true;
        if (pu.getNumberOfBackups() > 0) {
            for (int instanceId = 1 ; hasMinimum && instanceId <= pu.getNumberOfInstances() ; instanceId ++) {
                if (!isProcessingUnitPartitionHasMinimumNumberOfInstances(pu, instanceId, containers, minimumNumberOfInstancesPerPartition)) {
                    hasMinimum = false;
                    break;
                }
            }
        }
        else {
            hasMinimum = isProcessingUnitIntact(pu,containers);
        }
    
        return hasMinimum;
    }
    

    public static boolean isProcessingUnitPartitionIntact(ProcessingUnitInstance instance) {
        GridServiceContainer[] containers = instance.getAdmin().getGridServiceContainers().getContainers();
        return isProcessingUnitPartitionIntact(instance.getProcessingUnit(), instance.getInstanceId(), containers);
    }
    
    public static boolean isProcessingUnitIntact(ProcessingUnit pu) {
        return isProcessingUnitIntact(pu, pu.getAdmin().getGridServiceContainers().getContainers());
    }
    
    private static ProcessingUnitInstance findProcessingUnitInstance(ProcessingUnit pu, int instanceId, int backupId, GridServiceContainer[] containers) {
        for (final GridServiceContainer container : containers) {
            for (final ProcessingUnitInstance instance : container.getProcessingUnitInstances(pu.getName())) {
                if (instance.getInstanceId() == instanceId &&
                    instance.getBackupId() == backupId){
                    return instance;                    
                }
            }
        }
        return null;
    }
    
    public static boolean isProcessingUnitPartitionIntact(ProcessingUnit pu,int instanceId, GridServiceContainer[] containers) {

        return isProcessingUnitPartitionHasMinimumNumberOfInstances(pu, instanceId, containers, 1+pu.getNumberOfBackups());
    }

    private static boolean isProcessingUnitPartitionHasMinimumNumberOfInstances(ProcessingUnit pu, int instanceId,
            GridServiceContainer[] containers, int minimumNumberOfInstancesPerPartition) {
        
        boolean hasMinimum = true;
        
        if (minimumNumberOfInstancesPerPartition >= 1) {
            
            int numberOfPrimaryInstances = 0;
            int numberOfBackupInstances = 0;
            
            for (int backupId = 0 ; backupId <= pu.getNumberOfBackups() ; backupId++) {
                ProcessingUnitInstance instance = findProcessingUnitInstance(pu, instanceId, backupId, containers);
                if (instance != null &&
                        instance.getSpaceInstance() != null) {
                    
                    if (instance.getSpaceInstance().getMode() == SpaceMode.BACKUP) {
                        numberOfBackupInstances++;
                    }
                    else if (instance.getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                        numberOfPrimaryInstances++;
                    }
                }
            }
            
            hasMinimum = 
                    numberOfPrimaryInstances == 1 && 
                    1+numberOfBackupInstances >= minimumNumberOfInstancesPerPartition;
        }
        return hasMinimum;
    }
    
    /**
     * @param instance
     * @return all instances from the same partition that is not the specified instance.
     */
    public static Set<ProcessingUnitInstance> getOtherInstancesFromSamePartition(ProcessingUnitInstance instance) {
        
        final Set<ProcessingUnitInstance> puInstancesFromSamePartition = new HashSet<ProcessingUnitInstance>();
        for (final GridServiceContainer container : instance.getAdmin().getGridServiceContainers()) {
            puInstancesFromSamePartition.addAll(getOtherInstancesFromSamePartitionInContainer(container,instance));
        }
        return puInstancesFromSamePartition;
    }
    
    public static Set<ProcessingUnitInstance> getOtherInstancesFromSamePartitionInContainer(
            GridServiceContainer container, ProcessingUnitInstance instance) {
        Set<ProcessingUnitInstance> puInstancesFromSamePartition = new HashSet<ProcessingUnitInstance>();
        for (ProcessingUnitInstance instanceOnContainer : container.getProcessingUnitInstances(instance.getName())) {
            if (instanceOnContainer.getInstanceId() == instance.getInstanceId() &&
                !instanceOnContainer.equals(instance)) {
                
                puInstancesFromSamePartition.add(instanceOnContainer);
            }
        }
        return puInstancesFromSamePartition;
    }


    public static Set<ProcessingUnitInstance> getOtherInstancesFromSamePartitionInMachine(
            Machine machine, ProcessingUnitInstance puInstance) {
        final Set<ProcessingUnitInstance> puInstancesFromSamePartition = new HashSet<ProcessingUnitInstance>();
        for (final GridServiceContainer container : machine.getGridServiceContainers()) {
            puInstancesFromSamePartition.addAll(getOtherInstancesFromSamePartitionInContainer(container,puInstance));
        }
        return puInstancesFromSamePartition;
    }
    
    public static boolean isEvenlyDistributedAcrossMachines(ProcessingUnit pu, CapacityRequirementsPerAgent aggregatedAllocatedCapacity) {

        boolean isEvenlyDistributedAcrossMachines = true;
        final Machine[] machines = getMachinesFromAgentUids(pu, aggregatedAllocatedCapacity.getAgentUids());

        if (!isProcessingUnitIntact(pu,machines)) {
            isEvenlyDistributedAcrossMachines  = false;
        }
        else {
            Fraction averageCpuCoresPerPrimaryInstance = 
                getAverageCpuCoresPerPrimary(pu, aggregatedAllocatedCapacity);
            
            for (Machine source : machines) {
                for (Machine target : machines) {
                    
                    if (target.equals(source)) {
                        continue;
                    }
                    
                    if (isRestartRecommended(pu, source, target, averageCpuCoresPerPrimaryInstance, aggregatedAllocatedCapacity)) {
                        isEvenlyDistributedAcrossMachines = false;
                        break;
                    }
                }
            }
        }
        return isEvenlyDistributedAcrossMachines;
    }

    private static Machine[] getMachinesFromAgentUids(ProcessingUnit pu, Collection<String> agentUids) {
        final List<Machine> machines = new ArrayList<Machine>();
        final GridServiceAgents gridServiceAgents = pu.getAdmin().getGridServiceAgents();
        for (final String agentUid : agentUids) {
            final GridServiceAgent agent = gridServiceAgents.getAgentByUID(agentUid);
            if (agent == null) {
                throw new IllegalStateException("At this point agent " + agentUid + " must be discovered.");
            }
            machines.add(agent.getMachine());
        }
        return machines.toArray(new Machine[machines.size()]);
    }
    
    public static boolean isRestartRecommended(ProcessingUnit pu, Machine source, Machine target, Fraction optimalCpuCoresPerPrimary, CapacityRequirementsPerAgent allocatedCapacity) {       

        boolean isRestartRecommended = false;
        final int numberOfPrimaryInstancesOnSource = getNumberOfPrimaryInstancesOnMachine(pu, source);
        if (numberOfPrimaryInstancesOnSource > 0) {
            
        final int numberOfPrimaryInstancesOnTarget = getNumberOfPrimaryInstancesOnMachine(pu, target);
        Fraction cpuCoresOnSource = getNumberOfCpuCores(source, allocatedCapacity);
        Fraction cpuCoresOnTarget = getNumberOfCpuCores(target, allocatedCapacity);
        final Fraction missingCpuCoresBeforeRestart = 
            max(Fraction.ZERO, 
                optimalCpuCoresPerPrimary.multiply(numberOfPrimaryInstancesOnSource)
                .subtract(cpuCoresOnSource))
            .add(
            max(Fraction.ZERO, 
                optimalCpuCoresPerPrimary.multiply(numberOfPrimaryInstancesOnTarget)
                .subtract(cpuCoresOnTarget)));
        
        
        final Fraction missingCpuCoresAfterRestart = 
            max(Fraction.ZERO,
                optimalCpuCoresPerPrimary.multiply(numberOfPrimaryInstancesOnSource-1)
                .subtract(cpuCoresOnSource))
            .add(
            max(Fraction.ZERO,
                    optimalCpuCoresPerPrimary.multiply(numberOfPrimaryInstancesOnTarget+1)
                    .subtract(cpuCoresOnTarget)));
        
            isRestartRecommended = missingCpuCoresAfterRestart.compareTo(missingCpuCoresBeforeRestart) < 0;
        }
        
        return isRestartRecommended;
    }
    
    private static Fraction max(Fraction a, Fraction b) {
        if (b.compareTo(a) > 0) {
            return b;
        }
        return a;
    }

    /**
     * @return true if number of primary instances are evenly distributed across the specified machines
     *
    public static boolean isEvenlyDistributedAcrossMachines(ProcessingUnit pu, Machine[] machines) {
        
        if (!isProcessingUnitIntact(pu,machines)) {
            return false;
        }
        
        double averagePrimariesPerCpuCore = 
            getAverageNumberOfPrimaryInstancesPerCpuCore(pu,machines);
        
        boolean foundMachineWithSurplusPrimaries = false;
        boolean foundMachineWithDeficitPrimaries = false;
        
        for (Machine machine : machines) {
            
            if (RebalancingUtils.getAverageNumberOfPrimaryInstancesMinusOnePerCpuCore(pu,machine) 
                    >= averagePrimariesPerCpuCore) {
                foundMachineWithSurplusPrimaries = true;
            }
            else if (RebalancingUtils.getAverageNumberOfPrimaryInstancesPlusOnePerCpuCore(pu,machine) 
                    <= averagePrimariesPerCpuCore) {
                foundMachineWithDeficitPrimaries = true;
            }            
        }
        return 
        // everything is balanced
        (!foundMachineWithSurplusPrimaries && !foundMachineWithDeficitPrimaries) ||
        
        //not exactly balanced, but there is nothing we can do about it
        (!foundMachineWithSurplusPrimaries &&  foundMachineWithDeficitPrimaries) ||
        ( foundMachineWithSurplusPrimaries && !foundMachineWithDeficitPrimaries);
    }
*/
    private static boolean isProcessingUnitIntact(ProcessingUnit pu, Machine[] machines) {
        return isProcessingUnitIntact(pu, getContainersOnMachines(pu,machines));
    }
    
    /**
     * @return all containers that the gsm can deploy the specified pu.
     */
    public static GridServiceContainer[] getContainersOnMachines(ProcessingUnit pu) {
        return getContainersOnMachines(pu, pu.getAdmin().getMachines().getMachines());
    }
    
    private static GridServiceContainer[] getContainersOnMachines(ProcessingUnit pu, Machine[] machines) {
        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("Processing Unit must have exactly one container zone defined.");
        }
        final List<GridServiceContainer> containers = new ArrayList<GridServiceContainer>();
        for (final Machine machine : machines) {
            for (final GridServiceContainer container : machine.getGridServiceContainers()) {
                if (container.getZones().size() == 1 && 
                    container.getZones().containsKey(pu.getRequiredZones()[0])) {
                
                    containers.add(container);    
                }
            }
        }
        return containers.toArray(new GridServiceContainer[containers.size()]);
    }


    /**
     * @return true if number of instances are evenly distributed across the specified containers
     */
    public static boolean isEvenlyDistributedAcrossContainers(ProcessingUnit pu, GridServiceContainer[] containers) {
        
        if (!isProcessingUnitIntact(pu,containers)) {
            return false;
        }
        
        boolean evenlyDistributed = true;
        int numberOfInstances = pu.getTotalNumberOfInstances();
        int numberOfContainers = containers.length;
        if (numberOfInstances < numberOfContainers) {
            evenlyDistributed = false;
        }
        else {
            double expectedAverageNumberOfInstancesPerContainer = 1.0 * numberOfInstances / numberOfContainers;
            int numberOfServicesPerContainerUpperBound = (int) Math.ceil(expectedAverageNumberOfInstancesPerContainer);
            int numberOfServicesPerContainerLowerBound = (int) Math.floor(expectedAverageNumberOfInstancesPerContainer);
            
            for (GridServiceContainer container : containers) {
                
                int puNumberOfInstances = container.getProcessingUnitInstances(pu.getName()).length;
                
                if (puNumberOfInstances < numberOfServicesPerContainerLowerBound ||
                    puNumberOfInstances > numberOfServicesPerContainerUpperBound) {
                    evenlyDistributed = false;
                    break;
                }
            }
        }
        return evenlyDistributed;
    }


    public static Machine[] getMachinesHostingContainers(GridServiceContainer[] containers) {
        Set<Machine> machines = new HashSet<Machine>();
        for (GridServiceContainer container : containers) {
            machines.add(container.getMachine());
        }
        return machines.toArray(new Machine[machines.size()]);
    }
    
    /**
     * 
     * @param container - the container for which planned min number of instances is requested
     * @param approvedContainers - the containers approved for deployment for the specified pu
     * @param pu - the processing unit
     * @return the planned minimum number of instances for the specified container
     */
    public static int getPlannedMinimumNumberOfInstancesForContainer(
            GridServiceContainer container,
            GridServiceContainer[] approvedContainers,
            ProcessingUnit pu) {
        
        int min = 0;
        if (Arrays.asList(approvedContainers).contains(container)) {
            min = (int) Math.floor(
                    getAverageNumberOfInstancesPerContainer(approvedContainers, pu));
        }
        return min;
    }
    
    /**
     * 
     * @param container - the container for which planned min number of instances is requested
     * @param approvedContainers - the containers approved for deployment for the specified pu
     * @param pu - the processing unit
     * @return the planned minimum number of instances for the specified container
     */
    public static int getPlannedMaximumNumberOfInstancesForContainer(
            GridServiceContainer container,
            GridServiceContainer[] approvedContainers,
            ProcessingUnit pu) {
        
        int max = 0;
        if (Arrays.asList(approvedContainers).contains(container)) {
            double averageInstancesPerContainer = ((double) pu.getTotalNumberOfInstances()) / approvedContainers.length;
            max = (int) Math.ceil(averageInstancesPerContainer);
        }
        return max;
    }
    
    private static double getAverageNumberOfInstancesPerContainer(GridServiceContainer[] approvedContainers, ProcessingUnit pu) {
        final double averageInstancesPerContainer = ((double) pu.getTotalNumberOfInstances()) / approvedContainers.length;
        return averageInstancesPerContainer;
    }
    
    /**
     * Sorts all of the admin containers based on 
     * (number of instances from the specified pu - min number of instances)
     * If the container is not in the specified approved container list then min=0, meaning
     * it will get a higher weight in the sort. 
     * 
     * 
     * @param pu
     * @param approvedContainers
     * @return the list of sorted containers
     * @see RebalancingUtils#getPlannedMinimumNumberOfInstancesForContainer(GridServiceContainer, GridServiceContainer[], ProcessingUnit)
     */
    public static List<GridServiceContainer> sortAllContainersByNumberOfInstancesAboveMinimum(
            final ProcessingUnit pu, final GridServiceContainer[] approvedContainers) {
        final List<GridServiceContainer> sortedContainers = 
            new ArrayList<GridServiceContainer>(Arrays.asList(
                    pu.getAdmin().getGridServiceContainers().getContainers()));
        Collections.sort(sortedContainers,
            new Comparator<GridServiceContainer>() {

            public int compare(final GridServiceContainer o1, final GridServiceContainer o2) {
                return getNormalizedNumberOfInstances(o1)
                        - getNormalizedNumberOfInstances(o2);
            }
            
            private int getNormalizedNumberOfInstances(final GridServiceContainer container) {
                final int numberOfInstances = container.getProcessingUnitInstances(pu.getName()).length;
                return numberOfInstances - RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(container, approvedContainers, pu);
            }
        });
        return sortedContainers;
    }

    public static List<Machine> sortMachinesByNumberOfPrimaryInstancesPerCpuCore(
            final ProcessingUnit pu,
            final Machine[] machines,
            final CapacityRequirementsPerAgent allocatedCapacity) {
        
        final List<Machine> sortedMachines = 
                new ArrayList<Machine>(Arrays.asList(machines));
        
        Collections.sort(sortedMachines,
                new Comparator<Machine>() {

                public int compare(final Machine m1, final Machine m2) {
                    
                    if (getNumberOfCpuCores(m1,allocatedCapacity).equals(Fraction.ZERO) ||
                        getNumberOfCpuCores(m2,allocatedCapacity).equals(Fraction.ZERO)) {
                        throw new IllegalStateException("Rebalancing assumes positibe number of CPU cores per machine");
                    }
                    
                    return 
                        getNumberOfPrimaryInstancesPerCpuCore(pu,m1,allocatedCapacity)
                        .compareTo(
                        getNumberOfPrimaryInstancesPerCpuCore(pu,m2,allocatedCapacity));
                }
            });
            return sortedMachines;
        }

    public static Fraction getNumberOfPrimaryInstancesPerCpuCore(ProcessingUnit pu, Machine machine, CapacityRequirementsPerAgent allocatedCapacity) {
        return new Fraction(getNumberOfPrimaryInstancesOnMachine(pu, machine))
               .divide(getNumberOfCpuCores(machine,allocatedCapacity));
    }


    public static int getNumberOfPrimaryInstancesOnMachine(ProcessingUnit pu, Machine machine) {
        int numberOfPrimaryInstances = 0;
        for (GridServiceContainer container : machine.getGridServiceContainers()) {
            for (ProcessingUnitInstance instance : container.getProcessingUnitInstances(pu.getName())) {
                if (instance.getSpaceInstance() != null && 
                    instance.getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                    numberOfPrimaryInstances++;
                }
            }
        }
        return numberOfPrimaryInstances;
    }    

    public static FutureStatefulProcessingUnitInstance restartProcessingUnitInstanceAsync(
            ProcessingUnitInstance candidateInstance, Log logger, int timeout, TimeUnit timeUnit) {
        
        return relocateProcessingUnitInstanceAsync(
                candidateInstance.getGridServiceContainer(), 
                candidateInstance,
                logger,
                timeout, timeUnit);
    }


    public static Fraction getAverageCpuCoresPerPrimary(
            ProcessingUnit pu, CapacityRequirementsPerAgent aggregatedAllocatedCapacity) {
        
        CapacityRequirements totalAllocatedCapacity = aggregatedAllocatedCapacity.getTotalAllocatedCapacity();
        
        if (totalAllocatedCapacity.equalsZero()) {
            throw new IllegalStateException("allocated capacity cannot be empty.");
        }
        
        return getCpuCores(totalAllocatedCapacity).divide(pu.getNumberOfInstances());
    }
    
    private static Fraction getCpuCores(CapacityRequirements totalAllocatedCapacity) {
        return totalAllocatedCapacity.getRequirement(new CpuCapacityRequirement().getType()).getCpu();
    }

    public static Fraction getNumberOfCpuCores(Machine machine, CapacityRequirementsPerAgent allocatedCapacity) {
        if (machine.getGridServiceAgents().getSize() != 1) {
            throw new IllegalStateException("Machine must have at least one agent");
        }
        return getCpuCores(allocatedCapacity.getAgentCapacity(machine.getGridServiceAgent().getUid()));
    }
    
    public static String puInstanceToString(ProcessingUnitInstance instance) {
        StringBuilder builder = new StringBuilder(16);
        builder.append("[").append(instance.getInstanceId()).append(",").append(instance.getBackupId() + 1);
        SpaceInstance spaceInstance = instance.getSpaceInstance();
        if (spaceInstance != null) {
            builder.append(",").append(spaceInstance.getMode());
        }
        builder.append("]");
        return builder.toString();
    }
    
    public static String machineToString(Machine machine) {
        return machine.getHostName() + "/" + machine.getHostAddress();
    }
    
    public static String gscToString(GridComponent container) {
        return ContainersSlaUtils.gscToString(container);
    }

    public static String gscsToString(List<GridServiceContainer> containers) {
        return ContainersSlaUtils.gscsToString(containers);
    }

    public static String processingUnitDeploymentToString(ProcessingUnit pu) {
        StringBuilder deployment = new StringBuilder();
        for (final GridServiceContainer container : pu.getAdmin().getGridServiceContainers()) {
            deployment.append(gscToString(container));
            deployment.append(" { ");
            for (final ProcessingUnitInstance instance : container.getProcessingUnitInstances(pu.getName())) {
                deployment.append(puInstanceToString(instance));
                deployment.append(" ");
            }
            deployment.append(" } ");
        }
        return deployment.toString();
    }
    
}
