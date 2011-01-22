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
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
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

    // tracing used for component testing expected result validation
    private final List<FutureProcessingUnitInstance> doneFutureRelocations;
    private boolean tracingEnabled = false;
    public void enableTracing() {
        tracingEnabled = true;
    }
    public List<FutureProcessingUnitInstance> getTrace() {
        return doneFutureRelocations;
    }
    
    private static final Log logger = LogFactory.getLog(RebalancingSlaEnforcement.class);

    private static final int RELOCATION_TIMEOUT_FAILURE_SECONDS = 3600; // one hour
    private static final int RELOCATION_TIMEOUT_FAILURE_FORGET_SECONDS = 3600; // one hour

    private final Map<ProcessingUnit, List<FutureProcessingUnitInstance>> futureRelocationPerProcessingUnit;
    private final List<FutureProcessingUnitInstance> failedRelocations;

    private final HashMap<ProcessingUnit, RebalancingSlaEnforcementEndpoint> endpoints;

    public RebalancingSlaEnforcement() {
        futureRelocationPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureProcessingUnitInstance>>();
        failedRelocations = new ArrayList<FutureProcessingUnitInstance>();
        this.endpoints = new HashMap<ProcessingUnit, RebalancingSlaEnforcementEndpoint>();
        
        doneFutureRelocations = new ArrayList<FutureProcessingUnitInstance>();
    }

    public void destroy() {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }

    public RebalancingSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {

        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("Processing Unit must have exactly one container zone defined.");
        }

        if (!isEndpointDestroyed(pu)) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for the pu already exists.");
        }

        ProcessingUnit otherPu1 = getEndpointsWithSameNameAs(pu);
        if (otherPu1 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same name already exists.");
        }

        ProcessingUnit otherPu2 = getEndpointsWithSameContainersZoneAs(pu);
        if (otherPu2 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same (containers) zone already exists: "
                    + otherPu2.getName());
        }

        RebalancingSlaEnforcementEndpoint endpoint = new DefaultRebalancingSlaEnforcementEndpoint(pu);

        endpoints.put(pu, endpoint);
        futureRelocationPerProcessingUnit.put(pu, new ArrayList<FutureProcessingUnitInstance>());
        return endpoint;
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
            // maximumNumberOfConcurrentRelocationsPerMachine is disabled
            maximumNumberOfConcurrentRelocationsPerMachine = Integer.MAX_VALUE;
        }

        int concurrentRelocationsInContainer = 0;

        for (FutureProcessingUnitInstance future : getAllFutureProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();
            GridServiceContainer sourceContainer = future.getSourceContainer();
            List<GridServiceContainer> replicationSourceContainers = Arrays.asList(future.getReplicaitonSourceContainers());

            if (sourceContainer.equals(container) || // wrong reading of #instances on source
                    targetContainer.equals(container) || // wrong reading of #instances on target
                    replicationSourceContainers.contains(container)) { // replication source is busy
                                                                       // now with sending data to
                                                                       // the new backup

                concurrentRelocationsInContainer++;
            }
        }

        return concurrentRelocationsInContainer > 0 ||

        isConflictingOperationInProgress(container.getMachine(), maximumNumberOfConcurrentRelocationsPerMachine);
    }

    private boolean isConflictingOperationInProgress(Machine machine, int maximumNumberOfConcurrentRelocationsPerMachine) {

        if (maximumNumberOfConcurrentRelocationsPerMachine <= 0) {
            // maximumNumberOfConcurrentRelocationsPerMachine is disabled
            maximumNumberOfConcurrentRelocationsPerMachine = Integer.MAX_VALUE;
        }

        int concurrentRelocationsInMachine = 0;

        for (FutureProcessingUnitInstance future : getAllFutureProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();
            List<GridServiceContainer> replicationSourceContainers = Arrays.asList(future.getReplicaitonSourceContainers());

            Machine targetMachine = targetContainer.getMachine();
            Set<Machine> replicaitonSourceMachines = new HashSet<Machine>();
            for (GridServiceContainer replicationSourceContainer : replicationSourceContainers) {
                replicaitonSourceMachines.add(replicationSourceContainer.getMachine());
            }

            if (targetMachine.equals(machine) || // target machine is busy with replication
                    replicaitonSourceMachines.contains(machine)) { // replication source machine is
                                                                   // busy with replication

                concurrentRelocationsInMachine++;
            }
        }

        return concurrentRelocationsInMachine >= maximumNumberOfConcurrentRelocationsPerMachine;
    }

    public void destroyEndpoint(ProcessingUnit pu) {
        futureRelocationPerProcessingUnit.remove(pu);
        endpoints.remove(pu);
    }

    @SuppressWarnings("serial")
    class ConflictingOperationInProgressException extends Exception {
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
        return !endpoints.containsKey(pu) || futureRelocationPerProcessingUnit.get(pu) == null;
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

    private static String getContainerZone(ProcessingUnit pu) {
        String[] zones = pu.getRequiredZones();
        if (zones.length == 0) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. "
                    + pu.getName() + " has been deployed with no zones defined.");
        }

        if (zones.length > 1) {
            throw new IllegalArgumentException("Elastic Processing Unit must have exactly one (container) zone. "
                    + pu.getName() + " has been deployed with " + zones.length + " zones : " + Arrays.toString(zones));
        }

        return zones[0];
    }

    class DefaultRebalancingSlaEnforcementEndpoint implements RebalancingSlaEnforcementEndpoint {

        private final ProcessingUnit pu;
        
        // restart a primary as a last resort continuation state
        // when primary rebalancing algorithm fails, we use this state to restart primaries by partition number (hueristics)
        private int lastResortPartitionRestart = 0;
        private int lastResortPartitionRelocate = 0;

        private final Log logger;

        DefaultRebalancingSlaEnforcementEndpoint(ProcessingUnit pu) {
            this.pu = pu;
            this.logger = 
                new LogPerProcessingUnit(
                        new SingleThreadedPollingLog(
                                RebalancingSlaEnforcement.logger),
                        pu);
        }

        public ProcessingUnit getId() {
            return pu;
        }

        public boolean enforceSla(RebalancingSlaPolicy sla)
                throws ServiceLevelAgreementEnforcementEndpointDestroyedException {

            if (sla == null) {
                throw new IllegalArgumentException("sla cannot be null");
            }

            validateEndpointNotDestroyed(pu);

            String zone = pu.getRequiredZones()[0];

            for (GridServiceContainer container : sla.getContainers()) {
                Set<String> zones = container.getZones().keySet();

                if (zones.size() != 1) {
                    throw new IllegalArgumentException("Container " + RebalancingUtils.gscToString(container)
                            + " must have exactly one zone.");
                }

                if (!zones.contains(zone)) {
                    throw new IllegalArgumentException("Container " + RebalancingUtils.gscToString(container)
                            + " must have the zone " + zone);
                }
            }

            try {
                enforceSlaInternal(sla);
                logger.debug("Number of relocations in progress is " + futureRelocationPerProcessingUnit.get(pu).size());
                return isBalanced(sla);

            } catch (ConflictingOperationInProgressException e) {
                logger.debug(
                        "Cannot enforce Rebalancing SLA since a conflicting operation is in progress. Try again later.",
                        e);
                return false;
            }
        }

        private boolean isBalanced(RebalancingSlaPolicy sla) {

            return RebalancingUtils.isProcessingUnitIntact(pu, sla.getContainers())
                    &&

                    RebalancingUtils.isEvenlyDistributedAcrossContainers(pu, sla.getContainers())
                    &&

                    RebalancingUtils.isEvenlyDistributedAcrossMachines(pu,
                            RebalancingUtils.getMachinesHostingContainers(sla.getContainers()));
        }

        private void enforceSlaInternal(RebalancingSlaPolicy sla) throws ConflictingOperationInProgressException {

            cleanFutureRelocation();

            GridServiceContainer[] containers = sla.getContainers();
            if (pu.getNumberOfBackups() == 1) {
                // stage 1 : relocate backups so number of instances per container is balanced
                boolean relocateOnlyBackups = true;
                rebalanceNumberOfInstancesPerContainer(containers, sla, relocateOnlyBackups);

                if (!futureRelocationPerProcessingUnit.get(pu).isEmpty()) {
                    logger.debug("Rebalancing of backup instances is in progress after Stage 1. Try again later.");
                    return;
                }

                if (!RebalancingUtils.isProcessingUnitIntact(pu)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing Unit deployment is not intact after Stage 1. Try again later. " +
                                     RebalancingUtils.processingUnitDeploymentToString(pu) + 
                                     "Status = " + pu.getStatus());
                    }
                    return;
                }
                
                // if not all of pu instances are in the approved containers...
                // then skip directly to stage 3
                if (RebalancingUtils.isProcessingUnitIntact(pu, containers)) {

                    // stage 2: restart primaries so number of cpu cores per primary is balanced
                    rebalanceNumberOfPrimaryInstancesPerMachine(containers, sla);

                    if (!futureRelocationPerProcessingUnit.get(pu).isEmpty()) {
                        logger.debug("Restarting of primary instances is in progress after Stage 2. Try again later.");
                        return;
                    }
                    
                    if (!RebalancingUtils.isProcessingUnitIntact(pu)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Processing Unit deployment is not intact after Stage 2. Try again later. "+ 
                                         RebalancingUtils.processingUnitDeploymentToString(pu) + 
                                         "Status = " + pu.getStatus());
                        }
                        return;
                    }
                
                }
            }

            // stage 3: relocate backups or primaries so number of instances per container is
            // balanced
            boolean relocateOnlyBackups = false;
            rebalanceNumberOfInstancesPerContainer(containers, sla, relocateOnlyBackups);

        }

        /**
         * Invokes multiple relocation operations to balance number of pu instances per container.
         * 
         * @param containers
         * @param onlyBackups
         *            - perform only backup relocations.
         * 
         * @return future if performed relocation. null if no action needs to be performed.
         * 
         * @throws ConflictingOperationInProgressException
         *             - cannot determine what next to relocate since another conflicting operation
         *             is in progress.
         */
        private void rebalanceNumberOfInstancesPerContainer(GridServiceContainer[] containers,
                RebalancingSlaPolicy sla, boolean relocateOnlyBackups) throws ConflictingOperationInProgressException {

            while (true) {
                final FutureProcessingUnitInstance newInstance = rebalanceNumberOfInstancesPerContainerStep(
                        containers, relocateOnlyBackups, sla.getMaximumNumberOfConcurrentRelocationsPerMachine());

                if (newInstance == null) {
                    break;
                }

                futureRelocationPerProcessingUnit.get(pu).add(newInstance);
            }
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
         *             - cannot determine what to relocate since another conflicting operation is in
         *             progress.
         */
        private FutureProcessingUnitInstance rebalanceNumberOfInstancesPerContainerStep(
                final GridServiceContainer[] containers, boolean onlyBackups, int maximumNumberOfRelocationsPerMachine)
                throws ConflictingOperationInProgressException {

            // sort all containers (including those not in the specified containers
            // by (numberOfInstancesPerContainer - minNumberOfInstances)
            final List<GridServiceContainer> sortedContainers = RebalancingUtils.sortAllContainersByNumberOfInstancesAboveMinimum(
                    pu, containers);

            logger.debug("Containers sorted by number of instances above minimum: " + RebalancingUtils.gscsToString(sortedContainers));
            
            boolean conflict = false;
            // relocation is done from a source container with too many instances
            // to a target container with too little instances
            for (int targetIndex = 0; targetIndex < sortedContainers.size(); targetIndex++) {

                GridServiceContainer target = sortedContainers.get(targetIndex);

                if (isConflictingOperationInProgress(target, maximumNumberOfRelocationsPerMachine)) {
                    conflict = true;
                    logger.debug("Cannot relocate instances to " + RebalancingUtils.gscToString(target)
                            + " since a conflicting relocation is already in progress.");
                    continue;
                }

                int instancesInTarget = target.getProcessingUnitInstances(pu.getName()).length;
                if (instancesInTarget >= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(target,
                        containers, pu)) {
                    // target cannot host any more instances
                    // since the array is sorted there is no point in continuing the search
                    break;
                }

                for (int sourceIndex = sortedContainers.size() - 1; sourceIndex > targetIndex; sourceIndex--) {

                    GridServiceContainer source = sortedContainers.get(sourceIndex);

                    if (isConflictingOperationInProgress(source, maximumNumberOfRelocationsPerMachine)) {
                        conflict = true;
                        logger.debug("Cannot relocate instances from " + RebalancingUtils.gscToString(source)
                                + " since a conflicting relocation is already in progress.");
                        continue;
                    }

                    int instancesInSource = source.getProcessingUnitInstances(pu.getName()).length;
                    if (instancesInSource <= RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(source,
                            containers, pu)) {
                        // source cannot give up any instances
                        // since the array is sorted there is no point in continuing the search
                        break;
                    }

                    if (instancesInTarget >= RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(target,containers, pu)
                        && 
                        instancesInSource <= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(source, containers, pu)) {
                        // both source and target are balanced.
                        // since array is sorted there is no point in continuing the search
                        // as this condition will hold true.
                        break;
                    }

                    // we have a target and a source container. 
                    // now let's decide which pu instance to relocate from source to target
                    for (ProcessingUnitInstance candidateInstance : source.getProcessingUnitInstances(pu.getName())) {

                        if (candidateInstance.getSpaceInstance() == null) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " since embedded space is not detected");
                            continue;
                        }

                        if (onlyBackups && candidateInstance.getSpaceInstance().getMode() != SpaceMode.BACKUP) {
                            logger.debug("Prefer not to relocate "
                                    + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " since it is not a backup, and backups are preffered for relocation");
                            continue;
                        }

                        if (!RebalancingUtils.isProcessingUnitPartitionIntact(candidateInstance.getPartition())) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " since instances from the same partition are missing");
                            conflict = true;
                            continue;
                        }

                        if (isConflictingOperationInProgress(candidateInstance)) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "since another instance from the same partition is being relocated");
                            conflict = true;
                            continue;
                        }

                        for (Machine sourceReplicationMachine : RebalancingUtils.getMachinesHostingContainers(RebalancingUtils.getReplicationSourceContainers(candidateInstance))) {
                            if (isConflictingOperationInProgress(sourceReplicationMachine,
                                    maximumNumberOfRelocationsPerMachine)) {
                                logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                        + " " + "since replication source is on machine "
                                        + RebalancingUtils.machineToString(sourceReplicationMachine) + " "
                                        + "which is busy with another relocation");
                                conflict = true;
                                continue;
                            }
                        }

                        // check limit of pu instances from same partition per container
                        if (pu.getMaxInstancesPerVM() > 0) {
                            int numberOfOtherInstancesFromPartitionInTargetContainer = RebalancingUtils.getOtherInstancesFromSamePartitionInContainer(
                                    target, candidateInstance)
                                .size();

                            if (numberOfOtherInstancesFromPartitionInTargetContainer >= pu.getMaxInstancesPerVM()) {
                                logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                        + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                        + "since container already hosts "
                                        + numberOfOtherInstancesFromPartitionInTargetContainer + " "
                                        + "instance(s) from the same partition.");
                                continue;
                            }
                        }

                        // check limit of pu instances from same partition per machine 
                        if (pu.getMaxInstancesPerMachine() > 0) {
                            int numberOfOtherInstancesFromPartitionInTargetMachine = 
                                RebalancingUtils.getOtherInstancesFromSamePartitionInMachine(
                                    target.getMachine(), candidateInstance)
                                .size();

                            if (numberOfOtherInstancesFromPartitionInTargetMachine >= pu.getMaxInstancesPerMachine()) {
                                logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                        + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                        + "since machine already contains "
                                        + numberOfOtherInstancesFromPartitionInTargetMachine + " "
                                        + "instance(s) from the same partition.");
                                continue;
                            }
                        }

                        logger.info("Relocating " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                                + "from " + RebalancingUtils.gscToString(source) + " " + "to "
                                + RebalancingUtils.gscToString(target));
                        return RebalancingUtils.relocateProcessingUnitInstanceAsync(target, candidateInstance,
                                RELOCATION_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);

                    }// for pu instance
                }// for source container
            }// for target container

            if (// we tried to relocate primaries
                !onlyBackups &&
                
                 // backup instances exist and they are the reason we are here due to max instances per machine limitation
                pu.getNumberOfBackups() > 0 &&
                
                // no future operations that may conflict
                futureRelocationPerProcessingUnit.get(pu).size() == 0 &&
                
                // all instances are deployed
                RebalancingUtils.isProcessingUnitIntact(pu) &&
                
                // we're not done rebalancing yet!
                !RebalancingUtils.isEvenlyDistributedAcrossContainers(pu, containers)) {
                
                logger.debug("Optimal rebalancing hueristics failed balancing instances per container in this deployment. "+
                "Performing non-optimal relocation heuristics. Starting with partition " + lastResortPartitionRelocate);

                // algorithm failed. we need to use heuristics.
                // The reason the algorithm failed is that the machine that has an empty spot also has instances from partition that prevent a relocation into that machine.
                // For example, the excess machine wants to relocate Primary1 but the empty GSC is on a machine that has Backup1.
                // The workaround is to relocate any backup from another machine to the empty GSC, and so the "emptiness" would move to that other machine.
                // we look for backups by their partition number to avoid an endless loop.

                for (; lastResortPartitionRelocate < pu.getNumberOfInstances() - 1; lastResortPartitionRelocate++) {

                    // find backup to relocate
                    ProcessingUnitInstance candidateInstance = pu.getPartition(lastResortPartitionRelocate).getBackup();
                    
                    GridServiceContainer source = candidateInstance.getGridServiceContainer();
                    
                    for (int targetIndex = 0; targetIndex < sortedContainers.size(); targetIndex++) {

                        GridServiceContainer target = sortedContainers.get(targetIndex);
                        
                        if (target.getMachine().equals(source.getMachine())) {
                            // there's no point in relocating a backup into the same machine
                            // since we want another machine to have an "empty" container. 
                            continue;
                        }

                        int instancesInTarget = target.getProcessingUnitInstances(pu.getName()).length;
                        if (instancesInTarget >= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(
                                target, containers, pu)) {
                            // target cannot host any more instances
                            continue;
                        }

                        // check limit of pu instances from same partition per container
                        if (pu.getMaxInstancesPerVM() > 0) {
                            int numberOfOtherInstancesFromPartitionInTargetContainer = RebalancingUtils.getOtherInstancesFromSamePartitionInContainer(
                                    target, candidateInstance)
                                .size();

                            if (numberOfOtherInstancesFromPartitionInTargetContainer >= pu.getMaxInstancesPerVM()) {
                                logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                        + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                        + "since container already hosts "
                                        + numberOfOtherInstancesFromPartitionInTargetContainer + " "
                                        + "instance(s) from the same partition.");
                                continue;
                            }
                        }

                        // check limit of pu instances from same partition per machine
                        if (pu.getMaxInstancesPerMachine() > 0) {
                            int numberOfOtherInstancesFromPartitionInTargetMachine = RebalancingUtils.getOtherInstancesFromSamePartitionInMachine(
                                    target.getMachine(), candidateInstance)
                                .size();

                            if (numberOfOtherInstancesFromPartitionInTargetMachine >= pu.getMaxInstancesPerMachine()) {
                                logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                        + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                        + "since machine already contains "
                                        + numberOfOtherInstancesFromPartitionInTargetMachine + " "
                                        + "instance(s) from the same partition.");
                                continue;
                            }
                        }

                        logger.info("Relocating " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                                + "from " + RebalancingUtils.gscToString(source)
                                + " " + "to " + RebalancingUtils.gscToString(target));
                        return RebalancingUtils.relocateProcessingUnitInstanceAsync(target, candidateInstance,
                                RELOCATION_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);

                    }
                }

                // we haven't found any partition to relocate, probably the instance that requires
                // relocation has a partition lower than lastResortPartitionRelocate.

                if (lastResortPartitionRelocate >= pu.getNumberOfInstances() - 1) {
                    lastResortPartitionRelocate = 0; // better luck next time. continuation programming
                }
            }
            
            if (conflict) {
                throw new ConflictingOperationInProgressException();
            }

            
            return null;
        }

        /**
         * Makes sure that across machines the number of primary instances divided by the number of containers is balanced.  
         * @param containers
         * @param sla
         * @throws ConflictingOperationInProgressException
         */
        private void rebalanceNumberOfPrimaryInstancesPerMachine(GridServiceContainer[] containers,
                RebalancingSlaPolicy sla) throws ConflictingOperationInProgressException {

            while (true) {
                final FutureProcessingUnitInstance newInstance = rebalanceNumberOfPrimaryInstancesPerCpuCoreStep(
                        containers, sla.getMaximumNumberOfConcurrentRelocationsPerMachine());

                if (newInstance == null) {
                    break;
                }

                futureRelocationPerProcessingUnit.get(pu).add(newInstance);
            }
        }

        /**
         * Restarts one pu so that the number of primary instances divided by the number of containers is more balanced.  
         * @param containers
         * @param sla
         * @throws ConflictingOperationInProgressException
         */
        private FutureProcessingUnitInstance rebalanceNumberOfPrimaryInstancesPerCpuCoreStep(
                GridServiceContainer[] containers, int maximumNumberOfRelocationsPerMachine)
                throws ConflictingOperationInProgressException {

            // sort all machines (including those not in the specified containers)
            // by (numberOfPrimaryInstancesPerMachine - minNumberOfPrimaryInstances)
            // meaning machines that need primaries the most are first.
            Machine[] machines = RebalancingUtils.getMachinesHostingContainers(containers);
            final List<Machine> sortedMachines = 
                RebalancingUtils.sortMachinesByNumberOfPrimaryInstancesPerCpuCore(
                    pu, 
                    machines);

            double optimalCpuCoresPerPrimary = 
                RebalancingUtils.getAverageCpuCoresPerPrimary(pu,containers);
            boolean conflict = false;
            // the source machine is the machine where the primary is restarted (high primaries per core)
            // the target machine is the machine where a new primary is elected (low primaries per core)
            // try to match a source container with a target container and then do a primary restart.
            for (int targetIndex = 0; targetIndex < sortedMachines.size(); targetIndex++) {

                Machine target = sortedMachines.get(targetIndex);

                for (int sourceIndex = sortedMachines.size() - 1; sourceIndex > targetIndex; sourceIndex--) {

                    Machine source = sortedMachines.get(sourceIndex);

                    if (!RebalancingUtils.isRestartRecommended(pu, source, target, optimalCpuCoresPerPrimary)) {
                        // source cannot give up any primary instances
                        // since the array is sorted there is no point in continuing the search
                        break;
                    }
                    
                    if (isConflictingOperationInProgress(target, 1)) {
                        // number of primaries on machine might be skewed.
                        conflict = true;
                        logger.debug("Cannot restart a primary instance whos backup is on machine "
                                + RebalancingUtils.machineToString(target)
                                + " since a conflicting relocation is already in progress.");
                        continue;
                    }

                    if (isConflictingOperationInProgress(source, 1)) {
                        // number of primaries on machine might be skewed.
                        conflict = true;
                        logger.debug("Cannot restart a primary instance from machine "
                                + RebalancingUtils.machineToString(source)
                                + " since a conflicting relocation is already in progress.");
                        continue;
                    }
                    
                    // we have a target and a source container. 
                    // now all we need is a primary instance on the source container that has a backup on the target container
                    for (ProcessingUnitInstance candidateInstance : source.getProcessingUnitInstances(pu.getName())) {

                        if (candidateInstance.getSpaceInstance() == null) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " since embedded space is not detected");
                            continue;
                        }

                        if (candidateInstance.getSpaceInstance().getMode() != SpaceMode.PRIMARY) {
                            logger.debug("Cannot restart instance "
                                    + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " since it is not primary.");
                            continue;
                        }

                        if (!RebalancingUtils.isProcessingUnitPartitionIntact(candidateInstance.getPartition())) {
                            logger.debug("Cannot restart " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " since instances from the same partition are missing");
                            conflict = true;
                            continue;
                        }

                        if (isConflictingOperationInProgress(candidateInstance)) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "since another instance from the same partition is being relocated");
                            conflict = true;
                            continue;
                        }

                        Machine[] sourceReplicationMachines = RebalancingUtils.getMachinesHostingContainers(RebalancingUtils.getReplicationSourceContainers(candidateInstance));
                        if (sourceReplicationMachines.length > 1) {
                            throw new IllegalArgumentException("pu " + pu.getName() + " must have exactly one backup instance per partition in order for the primary restart algorithm to work.");
                        }
                        
                        if (!sourceReplicationMachines[0].equals(target)) {
                            logger.debug("Cannot restart " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + "since replication source is on "
                                    + RebalancingUtils.machineToString(sourceReplicationMachines[0]) + " "
                                    + "and not on the target machine " + RebalancingUtils.machineToString(target));
                            continue;
                        }
                    
                        if (logger.isInfoEnabled()) {
                            String sourceToString = RebalancingUtils.machineToString(source);
                            String targetToString = RebalancingUtils.machineToString(target);
                            int numberOfPrimaryInstancesOnTarget = RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, target);
                            int numberOfCpuCoresOnTarget = RebalancingUtils.getNumberOfCpuCores(target);
                            int numberOfPrimaryInstancesOnSource = RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, source);
                            int numberOfCpuCoresOnSource = RebalancingUtils.getNumberOfCpuCores(source);
                            logger.info(
                                "Restarting " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                                + "instance on machine " + sourceToString + " so that machine "
                                + sourceToString + " would have less instances per cpu core, and "
                                + targetToString + " would have more primary instances per cpu core. "
                                + sourceToString +" has " + numberOfPrimaryInstancesOnSource + " primary instances "+
                                "running on " + numberOfCpuCoresOnSource + " cpu cores. "
                                + targetToString +" has " + numberOfPrimaryInstancesOnTarget + " primary instances "+
                                "running on " + numberOfCpuCoresOnTarget + " cpu cores.");
                        }
                        
                        return RebalancingUtils.restartProcessingUnitInstanceAsync(candidateInstance,
                                RELOCATION_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);
                    }
                }
            }

            if (futureRelocationPerProcessingUnit.get(pu).size() == 0 &&
                RebalancingUtils.isProcessingUnitIntact(pu) &&
                RebalancingUtils.isEvenlyDistributedAcrossContainers(pu, containers) &&
                !RebalancingUtils.isEvenlyDistributedAcrossMachines(pu, machines)) {
                    
                    logger.debug("Optimal primary rebalancing hueristics failed balancing primaries in this deployment. "+
                                 "Performing non-optimal restart heuristics. Starting with partition " + lastResortPartitionRestart);
                    
                    //
                    // We cannot balance primaries per cpu core with one restart.
                    // That means we need forward looking logic for more than one step, which we currently haven't implemented.
                    // So we just restart a primary on a machine that has too many parimaries per cpu core.
                    // In order to make the algorithm deterministic and avoid loops we restart primaries by
                    // their natural order (by partition number)
                    //
                    // lastResortPartitionRestart is the next partition we should restart. 
                    for (;lastResortPartitionRestart < pu.getNumberOfInstances()-1 ; lastResortPartitionRestart++) {
                        
                        ProcessingUnitInstance candidateInstance = pu.getPartition(lastResortPartitionRestart).getPrimary();
                        Machine source = candidateInstance.getMachine();
                        
                        Machine[] sourceReplicationMachines = RebalancingUtils.getMachinesHostingContainers(RebalancingUtils.getReplicationSourceContainers(candidateInstance));
                        if (sourceReplicationMachines.length > 1) {
                            throw new IllegalArgumentException("pu " + pu.getName() + " must have exactly one backup instance per partition in order for the primary restart algorithm to work.");
                        }
                        
                        if (sourceReplicationMachines[0].equals(source)) {
                            logger.debug("Cannot restart " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + "since replication source is on same machine as primary, so restarting will have not change number of primaries on machine.");
                            continue;
                        }
                    
                        if (RebalancingUtils.getNumberOfCpuCores(source) <=
                                RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, source) * optimalCpuCoresPerPrimary) {
                            
                            // number of cores is below optimal, or put it another way there are too many primaries on the machine                        
                            if (logger.isInfoEnabled()) {
                                String sourceToString = RebalancingUtils.machineToString(source);
                                int numberOfPrimaryInstancesOnSource = RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, source);
                                int numberOfCpuCoresOnSource = RebalancingUtils.getNumberOfCpuCores(source);
                                logger.info(
                                    "Restarting " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                                    + "instance on machine " + sourceToString + " so that machine "
                                    + sourceToString + " would have less instances per cpu core. "
                                    + sourceToString +" has " + numberOfPrimaryInstancesOnSource + " primary instances "+
                                    "running on " + numberOfCpuCoresOnSource + " cpu cores. ");
                            }
                            
                            return RebalancingUtils.restartProcessingUnitInstanceAsync(candidateInstance,
                                    RELOCATION_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);
                        }
                    }
                    // we haven't found any partition to restart, probably the instance that requires restart
                    // has a partition lower than lastResortPartitionRestart.
                    
                    if (lastResortPartitionRestart >= pu.getNumberOfInstances()-1) {
                        lastResortPartitionRestart = 0; //better luck next time. continuation programming
                    }
            }                    

            if (conflict) {
                throw new ConflictingOperationInProgressException();
            }

            return null;
        }

        private void cleanFutureRelocation() {

            List<FutureProcessingUnitInstance> list = futureRelocationPerProcessingUnit.get(pu);

            if (list == null) {
                throw new IllegalStateException("endpoint for pu " + pu.getName() + " has already been destroyed.");
            }

            final Iterator<FutureProcessingUnitInstance> iterator = list.iterator();
            while (iterator.hasNext()) {
                FutureProcessingUnitInstance future = iterator.next();

                if (future.isDone()) {

                    if (tracingEnabled) {
                        doneFutureRelocations.add(future);
                    }
                    
                    iterator.remove();
                    
                    Exception exception = null;

                    try {
                        ProcessingUnitInstance puInstance = future.get();
                        logger.info("Processing unit relocation completed successfully "
                                + RebalancingUtils.puInstanceToString(puInstance));

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
                        logger.info(future.getFailureMessage(), exception);
                        failedRelocations.add(future);
                    }
                }
            }

            cleanFailedFutureRelocations();
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
    }
    
}
