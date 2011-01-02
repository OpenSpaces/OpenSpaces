package org.openspaces.grid.gsm.rebalancing;

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
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.grid.esm.ToStringHelper;
import org.openspaces.grid.gsm.machines.DefaultMachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

import com.gigaspaces.cluster.activeelection.SpaceMode;

/**
 * Implements the elastic re-balancing algorithm based on the specified SLA. Each endpoint is
 * dedicated to the rebalancing of one processing unit. The SLA specifies the containers that are
 * approved for the pu deployment, and the algorithm enforces equal spread of instances per
 * container, and the primary instances per machine.
 * 
 * @author itaif
 * 
 */
public class RebalancingSlaEnforcement implements
        ServiceLevelAgreementEnforcement<RebalancingSlaPolicy, ProcessingUnit, RebalancingSlaEnforcementEndpoint> {

    private static final Log logger = LogFactory.getLog(DefaultMachinesSlaEnforcementEndpoint.class);

    private static final int RELOCATION_TIMEOUT_FAILURE_SECONDS = 3600; // one hour
    private static final int RELOCATION_TIMEOUT_FAILURE_FORGET_SECONDS = 3600; // one hour

    private final Map<ProcessingUnit, List<FutureProcessingUnitInstance>> futureRelocationPerProcessingUnit;
    private final List<FutureProcessingUnitInstance> failedRelocations;

    private boolean destroyed;

    public RebalancingSlaEnforcement() {
        futureRelocationPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureProcessingUnitInstance>>();
        failedRelocations = new ArrayList<FutureProcessingUnitInstance>();
    }

    public void destroy() {
        destroyed = true;
    }

    public RebalancingSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {

        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("Processing Unit must have exactly one container zone defined.");
        }

        return new RebalancingSlaEnforcementEndpoint() {

            public ProcessingUnit getId() {
                return pu;
            }

            public boolean enforceSla(RebalancingSlaPolicy sla)
                    throws ServiceLevelAgreementEnforcementEndpointDestroyedException {

                if (sla == null) {
                    throw new IllegalArgumentException("sla cannot be null");
                }

                if (destroyed) {
                    throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
                }

                String zone = pu.getRequiredZones()[0];

                for (GridServiceContainer container : sla.getContainers()) {
                    Set<String> zones = container.getZones().keySet();

                    if (zones.size() != 1) {
                        throw new IllegalArgumentException("Container " + ToStringHelper.gscToString(container)
                                + " must have exactly one zone.");
                    }

                    if (!zones.contains(zone)) {
                        throw new IllegalArgumentException("Container " + ToStringHelper.gscToString(container)
                                + " must have the zone " + zone);
                    }
                }

                try {
                    enforceSlaInternal(pu, sla);
                    return isBalanced(pu, sla);

                } catch (ConflictingOperationInProgressException e) {
                    logger.debug(
                            "Cannot enforce Containers SLA since a conflicting operation is in progress. Try again later.",
                            e);
                    return false;
                }
            }

            private boolean isBalanced(final ProcessingUnit pu, RebalancingSlaPolicy sla) {

                return RebalancingUtils.isProcessingUnitIntact(pu, sla.getContainers())
                        &&

                        RebalancingUtils.isEvenlyDistributedAcrossContainers(pu, sla.getContainers())
                        &&

                        RebalancingUtils.isEvenlyDistributedAcrossMachines(pu,
                                RebalancingUtils.getMachinesHostingContainers(sla.getContainers()));
            }

            private void enforceSlaInternal(ProcessingUnit pu, RebalancingSlaPolicy sla)
                    throws ConflictingOperationInProgressException {

                cleanFutureRelocation(pu);

                GridServiceContainer[] containers = sla.getContainers();

                // stage 1 : relocate backups so number of instances per container is balanced
                boolean relocateOnlyBackups = true;
                rebalanceNumberOfInstancesPerContainer(pu, containers, sla, relocateOnlyBackups);
                                
                if ( !RebalancingUtils.isProcessingUnitIntact(pu) ||
                     !futureRelocationPerProcessingUnit.get(pu).isEmpty()) {
                    return;
                }
                  
                //stage 2: restart primaries so number of primaries per machine is balanced
                rebalanceNumberOfPrimaryInstancesPerMachine(pu,containers);
                
                if ( !RebalancingUtils.isProcessingUnitIntact(pu) ||
                     !futureRelocationPerProcessingUnit.get(pu).isEmpty()) {
                   return;
                }
         
                // stage 3: relocate backups or primaries so number of instances per container is balanced
                relocateOnlyBackups = false; 
                rebalanceNumberOfInstancesPerContainer(pu, containers, sla, relocateOnlyBackups);

            }

            private void rebalanceNumberOfInstancesPerContainer(
                    ProcessingUnit pu,
                    GridServiceContainer[] containers,
                    RebalancingSlaPolicy sla,
                    boolean relocateOnlyBackups)
                    throws ConflictingOperationInProgressException {
                
                while (true) {
                    final FutureProcessingUnitInstance newInstance =
                        rebalanceNumberOfInstancesPerContainerStep(
                                pu, 
                                containers, 
                                relocateOnlyBackups,
                                sla.getMaximumNumberOfConcurrentRelocationsPerMachine());
                    
                    if (newInstance == null) {
                        break;
                    }
                    
                    futureRelocationPerProcessingUnit.get(pu).add(newInstance);
                }
            }

            private void cleanFutureRelocation(ProcessingUnit pu) {

                List<FutureProcessingUnitInstance> list = futureRelocationPerProcessingUnit.get(pu);

                if (list == null) {
                    list = new ArrayList<FutureProcessingUnitInstance>();
                    futureRelocationPerProcessingUnit.put(pu, list);
                }

                final Iterator<FutureProcessingUnitInstance> iterator = list.iterator();
                while (iterator.hasNext()) {
                    FutureProcessingUnitInstance future = iterator.next();

                    if (future.isDone()) {

                        iterator.remove();

                        Exception exception = null;

                        try {
                            ProcessingUnitInstance puInstance = future.get();
                            logger.info("Processing unit relocation completed successfully "
                                    + ToStringHelper.puInstanceToString(puInstance));

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
                            logger.warn(future.getFailureMessage(), exception);
                            failedRelocations.add(future);
                        }
                    }
                }

                cleanFailedFutureRelocations();

            }

            /**
             * Invokes one relocation operations to balance number of instances per container
             * 
             * @param pu
             * @param containers
             * @param onlyBackups
             *            - perform only backup relocations.
             * 
             * @return future if performed relocation. null if no action needs to be performed.
             * 
             * @throws ConflictingOperationInProgressException
             *             - cannot determine what to relocate since another conflicting operation
             *             is in progress.
             */
            private FutureProcessingUnitInstance rebalanceNumberOfInstancesPerContainerStep(
                    final ProcessingUnit pu,
                    final GridServiceContainer[] containers, 
                    boolean onlyBackups, 
                    int maximumNumberOfRelocationsPerMachine)
                    throws ConflictingOperationInProgressException {

                // sort all containers (including those not in the specified containers 
                // by (numberOfInstancesPerContainer - minNumberOfInstances)
                final List<GridServiceContainer> sortedContainers = 
                    RebalancingUtils.sortAllContainersByNumberOfInstancesAboveMinimum(pu, containers);

                boolean conflict = false;
                // relocation is done from a source container with too many instances
                // to a target container with too little instances
                for (int targetIndex = 0; 
                     targetIndex < sortedContainers.size() ; 
                     targetIndex++) {

                    GridServiceContainer target = sortedContainers.get(targetIndex);

                    if (isConflictingOperationInProgress(target, maximumNumberOfRelocationsPerMachine)) {
                        conflict = true;
                        logger.debug("Cannot relocate instances to " + ToStringHelper.gscToString(target)
                                + " since a conflicting relocation is already in progress.");
                        continue;
                    }

                    int instancesInTarget = target.getProcessingUnitInstances(pu.getName()).length;
                    if (instancesInTarget >= 
                            RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(target, containers, pu)) {
                        // target cannot host any more instances
                        // since the array is sorted there is no point in continuing the search
                        break;
                    }

                    for (int sourceIndex = sortedContainers.size() - 1; 
                         sourceIndex > targetIndex ; 
                         sourceIndex--) {

                        GridServiceContainer source = sortedContainers.get(sourceIndex);

                        if (isConflictingOperationInProgress(source, maximumNumberOfRelocationsPerMachine)) {
                            conflict = true;
                            logger.debug("Cannot relocate instances from " + ToStringHelper.gscToString(source)
                                    + " since a conflicting relocation is already in progress.");
                            continue;
                        }

                        int instancesInSource = source.getProcessingUnitInstances(pu.getName()).length;
                        if (instancesInSource <= 
                                RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(source, containers, pu)) {
                            // source cannot give up any instances
                            // since the array is sorted there is no point in continuing the search
                            break;
                        }

                        if (instancesInTarget >= RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(target, containers, pu) &&
                            instancesInSource <= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(source, containers, pu)) {
                            // both source and target are balanced.
                            // since array is sorted there is no point in continuing the search
                            // as this condition will hold true.
                            break;
                        }
                        
                        // we have a target and a source container. now let's decide which pu instance to relocate
                        for (ProcessingUnitInstance candidateInstance : source.getProcessingUnitInstances(pu.getName())) {

                            if (candidateInstance.getSpaceInstance() == null) {
                                logger.debug("Cannot relocate " + ToStringHelper.puInstanceToString(candidateInstance)
                                        + " since embedded space is not detected");
                                continue;
                            }

                            if (onlyBackups && 
                                candidateInstance.getSpaceInstance().getMode() != SpaceMode.BACKUP) {
                                logger.debug("Prefer not to relocate "
                                        + ToStringHelper.puInstanceToString(candidateInstance)
                                        + " since it is not a backup, and backups are preffered for relocation");
                                continue;
                            }

                            if (!RebalancingUtils.isProcessingUnitPartitionIntact(candidateInstance.getPartition())) {
                                logger.debug("Cannot relocate " + ToStringHelper.puInstanceToString(candidateInstance)
                                        + " since instances from the same partition are missing");
                                conflict = true;
                                continue;
                            }
                            
                            if (isConflictingOperationInProgress(candidateInstance)) {
                                logger.debug("Cannot relocate " + ToStringHelper.puInstanceToString(candidateInstance)
                                        + " " + "since another instance from the same partition is being relocated");
                                conflict = true;
                                continue;
                            }
                            
                            if (pu.getMaxInstancesPerVM() > 0) {
                                int numberOfOtherInstancesFromPartitionInTargetContainer = 
                                    RebalancingUtils.getOtherInstancesFromSamePartitionInContainer(
                                        target, candidateInstance)
                                    .size();

                                if (numberOfOtherInstancesFromPartitionInTargetContainer >= 
                                    pu.getMaxInstancesPerVM()) {
                                    logger.debug("Cannot relocate "
                                            + ToStringHelper.puInstanceToString(candidateInstance) + " "
                                            + "to container " + ToStringHelper.gscToString(target) + " "
                                            + "since container already hosts "
                                            + numberOfOtherInstancesFromPartitionInTargetContainer + " "
                                            + "instance(s) from the same partition.");
                                    continue;
                                }
                            }

                            if (pu.getMaxInstancesPerMachine() > 0) {
                                int numberOfOtherInstancesFromPartitionInTargetMachine = 
                                    RebalancingUtils.getOtherInstancesFromSamePartitionInMachine(
                                        target.getMachine(), candidateInstance)
                                    .size();

                                if (numberOfOtherInstancesFromPartitionInTargetMachine >= 
                                    pu.getMaxInstancesPerMachine()) {
                                    logger.debug("Cannot relocate "
                                            + ToStringHelper.puInstanceToString(candidateInstance) + " "
                                            + "to container " + ToStringHelper.gscToString(target) + " "
                                            + "since machine already contains "
                                            + numberOfOtherInstancesFromPartitionInTargetMachine + " "
                                            + "instance(s) from the same partition.");
                                    continue;
                                }
                            }

                            logger.info("Relocating " + ToStringHelper.puInstanceToString(candidateInstance) + " "
                                    + "from " + ToStringHelper.gscToString(source) + " " + "to "
                                    + ToStringHelper.gscToString(target));
                            return 
                                RebalancingUtils.relocateProcessingUnitAsync(
                                        target,
                                        candidateInstance, 
                                        RELOCATION_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);


                        }// for pu instance
                    }// for source container
                }// for target container
                
                if (conflict) {
                    throw new ConflictingOperationInProgressException();
                }

                return null;
            }

            private void rebalanceNumberOfPrimaryInstancesPerMachine(
                    ProcessingUnit pu,
                    GridServiceContainer[] containers) {
                
                while (true) {
                    final FutureProcessingUnitInstance newInstance =
                        rebalanceNumberOfPrimaryInstancesPerMachineStep(
                                pu, 
                                containers);
                    
                    if (newInstance == null) {
                        break;
                    }
                    
                    futureRelocationPerProcessingUnit.get(pu).add(newInstance);
                }
            }

            private FutureProcessingUnitInstance rebalanceNumberOfPrimaryInstancesPerMachineStep(ProcessingUnit pu,
                    GridServiceContainer[] containers) {
                // TODO Auto-generated method stub
                return null;
            }
            
            

        };
    }

    /**
     * This method removes failed relocations from the list allowing a retry attempt to take place.
     * Some failures are removed immediately, while others stay in the list for
     * RELOCATION_TIMEOUT_FAILURE_IGNORE_SECONDS.
     */
    private void cleanFailedFutureRelocations() {

        List<FutureProcessingUnitInstance> list = failedRelocations;
        final Iterator<FutureProcessingUnitInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            FutureProcessingUnitInstance future = iterator.next();
            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp().getTime()) / 1000);

            if (future.getException() != null
                    && future.getException().getCause() instanceof WrongContainerRelocationException
                    && future.getTargetContainer().isDiscovered()
                    && passedSeconds < RELOCATION_TIMEOUT_FAILURE_FORGET_SECONDS) {

                // do not remove future from list since the target container did not have enough
                // memory
                // meaning something is very wrong with our assumptions on the target container.
                // We leave this future in the list so it will cause conflicting exceptions.
                // Once RELOCATION_TIMEOUT_FAILURE_FORGET_SECONDS passes it is removed from the
                // list.
            } else {
                logger.info("Forgetting relocation error " + future.getFailureMessage());
                iterator.remove();
            }
        }
    }

    private boolean isConflictingOperationInProgress(ProcessingUnitInstance candidateInstance) {

        for (FutureProcessingUnitInstance future : getAllFutureProcessingUnitInstances()) {
            if (future.getProcessingUnit().equals(candidateInstance.getProcessingUnit())
                    && future.getInstanceId() == candidateInstance.getInstanceId()) {
                return true;
            }
        }

        return false;
    }

    private List<FutureProcessingUnitInstance> getAllFutureProcessingUnitInstances() {
        final List<FutureProcessingUnitInstance> futures = new ArrayList<FutureProcessingUnitInstance>();

        for (final ProcessingUnit pu : this.futureRelocationPerProcessingUnit.keySet()) {
            for (final FutureProcessingUnitInstance future : this.futureRelocationPerProcessingUnit.get(pu)) {
                futures.add(future);
            }
        }

        for (final FutureProcessingUnitInstance future : this.failedRelocations) {
            futures.add(future);
        }
        return futures;
    }

    private boolean isConflictingOperationInProgress(GridServiceContainer container,
            int maximumNumberOfConcurrentRelocationsPerMachine) {

        if (maximumNumberOfConcurrentRelocationsPerMachine <= 0) {
            //maximumNumberOfConcurrentRelocationsPerMachine is disabled
            maximumNumberOfConcurrentRelocationsPerMachine = Integer.MAX_VALUE;
        }

        Machine machine = container.getMachine();
        int concurrentRelocationsInContainer = 0;
        int concurrentRelocationsInMachine = 0;

        for (FutureProcessingUnitInstance future : getAllFutureProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();
            GridServiceContainer sourceContainer = future.getSourceContainer();
            List<GridServiceContainer> replicationSourceContainers = Arrays.asList(future.getReplicaitonSourceContainers());

            if (    sourceContainer.equals(container) || // wrong reading of #instances on source
                    targetContainer.equals(container) || // wrong reading of #instances on target
                    replicationSourceContainers.contains(container)) { // replication source is busy now with sending data to the new backup

                concurrentRelocationsInContainer++;
            }

            Machine targetMachine = targetContainer.getMachine();
            Set<Machine> replicaitonSourceMachines = new HashSet<Machine>();
            for (GridServiceContainer replicationSourceContainer : replicationSourceContainers) {
                replicaitonSourceMachines.add(replicationSourceContainer.getMachine());
            }

            if (targetMachine.equals(machine) ||    // target machine is busy with replication 
                replicaitonSourceMachines.contains(machine)) { // source machine is busy with replication

                concurrentRelocationsInMachine++;
            }
        }

        return concurrentRelocationsInContainer > 0
                || concurrentRelocationsInMachine > maximumNumberOfConcurrentRelocationsPerMachine;
    }

    public void destroyEndpoint(ProcessingUnit id) {
        destroyed = true;
    }

    @SuppressWarnings("serial")
    class ConflictingOperationInProgressException extends Exception {
    }
}
