package org.openspaces.grid.gsm.machines;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;


/**
 * A greedy 2D bin packing algorithm.
 * 
 * @see org.openspaces.utest.grid.gsm.BinPackingSolverTest for usage examples
 * 
 * @author itaif
 *
 */
public class BinPackingSolver {

    private Log logger;
    private long containerMemoryCapacityInMB;
    private AggregatedAllocatedCapacity unallocatedCapacity;
    private AggregatedAllocatedCapacity allocatedCapacityForPu;
    private AggregatedAllocatedCapacity allocatedCapacityResult;
    private AggregatedAllocatedCapacity deallocatedCapacityResult;
    
    private long maxMemoryCapacityInMB;
    private int minimumNumberOfMachines;

    public BinPackingSolver() {
        allocatedCapacityResult = new AggregatedAllocatedCapacity();
        deallocatedCapacityResult = new AggregatedAllocatedCapacity();
    }
    
    /**
     * Used for high availability purposes to calculate maximum cpu and memory per machine.
     * For example if min number of machines is 2, then max memory per machine is half of the total memory
     * @param minimumNumberOfMachines
     */
    public void setMinimumNumberOfMachines(int minimumNumberOfMachines) {
        this.minimumNumberOfMachines = minimumNumberOfMachines;
    }
    
    /**
     * Sets the processing unit specific logger
     */
    public void setLogger(Log logger) {
        this.logger = logger;
    }

    /**
     * Sets the smallest unit of memory allocation.
     */
    public void setContainerMemoryCapacityInMB(long containerMemoryCapacityInMB) {
        this.containerMemoryCapacityInMB = containerMemoryCapacityInMB;
    }

    /**
     * Sets the remaining unallocated capacity on existing agents.
     */
    public void setUnallocatedCapacity(AggregatedAllocatedCapacity unallocatedCapacity) {
        this.unallocatedCapacity = unallocatedCapacity;
    }
    
    /**
     * Sets the currently allocated capacity of PU
     */
    public void setAllocatedCapacityForPu(AggregatedAllocatedCapacity allocatedCapacityForPu) {
        this.allocatedCapacityForPu = allocatedCapacityForPu;
    }


    /**
     * Sets the maximum total memory that can be allocated for PU
     */
    public void setMaxAllocatedMemoryCapacityOfPuInMB(long maxMemoryInMB) {
        this.maxMemoryCapacityInMB = maxMemoryInMB;
    }


    public void solveManualCapacityScaleIn(AllocatedCapacity capacityToDeallocate) {
        validate();
        
        AllocatedCapacity goalCapacity = getGoalCapacityScaleIn(capacityToDeallocate); // allocatedForPu - capacityToAllocate
        
        // try to remove complete machines then try to remove part of machines (containers)
        removeExcessMachines(goalCapacity);
        removeExcessContainers(goalCapacity);   
        
        // rebalance containers between machines (without adding/removing containers)
        rebalanceExistingContainers();
        
        if (logger.isDebugEnabled()) {
            logger.debug("BinPackingSolver: manual capacity scale in" + capacityToDeallocate + " allocatedCapacityResult=" + this.getAllocatedCapacityResult().toDetailedString() + " deallocatedCapacityResult="+this.getDeallocatedCapacityResult());
        }        
    }
    
    public void solveManualCapacityScaleOut(AllocatedCapacity capacityToAllocate) {
    
        validate();
                
        AllocatedCapacity goalCapacity = getGoalCapacityScaleOut(capacityToAllocate); // allocatedForPu + capacityToAllocate
        
        // do whatever we can to reach the target capacity.
        boolean startExcessContainersToSatisfyCpuShortage = false;
        // does not allocate a container just to satisfy cpu shortage, only to satisfy memory shortage.
        // but it does allocate cpu on machines with existing containers.
        allocateNewCapacityForPu(goalCapacity, startExcessContainersToSatisfyCpuShortage);
        
        // not enough cpu. 
        if (allocatedCapacityForPu.getTotalAllocatedCapacity().getCpuCores().compareTo(goalCapacity.getCpuCores()) < 0) {
            
            //try again, this time start containers for cpu purposes.
            startExcessContainersToSatisfyCpuShortage = true;
            allocateNewCapacityForPu(goalCapacity, startExcessContainersToSatisfyCpuShortage);
        }

        // accept the fact that we might not have enough capacity to reach the target
        // rebalance what we have now
        goalCapacity = lowestCommonGoal(goalCapacity,allocatedCapacityForPu.getTotalAllocatedCapacity());
        
        // try to remove complete machines then try to remove part of machines (containers)
        removeExcessMachines(goalCapacity);
        removeExcessContainers(goalCapacity);
        removeOverMaximumContainers();    
        
        // rebalance containers between machines (without adding/removing containers)
        rebalanceExistingContainers();
        
        if (logger.isDebugEnabled()) {
            logger.debug("BinPackingSolver: manual capacity scale out" + capacityToAllocate + " allocatedCapacityResult=" + this.getAllocatedCapacityResult().toDetailedString() + " deallocatedCapacityResult="+this.getDeallocatedCapacityResult());
        }
    }

    /**
     * Removes excess containers that are not needed. Remaining cpu capacity is spread accross other machines.
     * No machines are removed (never remove the last container on the machine)
     */
    private void removeExcessContainers(AllocatedCapacity goalCapacity) {
        
        if (!allocatedCapacityForPu.getTotalAllocatedCapacity().satisfies(goalCapacity)) {
            throw new IllegalStateException("Removing containers assumes that the goal " + goalCapacity +" is already satisfied.");
        }
        
        AllocatedCapacity oneContainer = new AllocatedCapacity(Fraction.ZERO, containerMemoryCapacityInMB);
        Fraction maxCpuCoresPerContainer = getMaximumCpuCoresPerContainer();
        boolean retry;
        do {
            retry = false;
            for (String agentUid : allocatedCapacityForPu.getAgentUids()) {               
        
                if (!allocatedCapacityForPu.getTotalAllocatedCapacity().subtractOrZero(oneContainer).satisfies(goalCapacity)) {
                    // cannot remove any container without breaching goal
                    break;
                }
                
                if (getNumberOfContainers(agentUid) <= 1) {
                    // cannot remove container without removing the machine
                    continue;
                }
            
                if (getCpuCoresPerContainer(agentUid,1).compareTo(maxCpuCoresPerContainer) > 0) {
                    // cannot remove container without introducing a container that works harder than the max
                    continue;
                }
                        
                // remove container
                deallocateCapacityOnMachine(agentUid, oneContainer);
                retry = true;
            }
        } while (retry);
        
        int excessNumberOfContainers = 
            (int) Math.floor(1.0*(allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() - goalCapacity.getMemoryInMB()) / containerMemoryCapacityInMB);
        
        if (excessNumberOfContainers < 1) {
            // cannot remove any container without breaching goal
            return;
        }
        
     // cpu per container is equal for all cpus. 
     // Try reduce the number of containers at the same time for all machines while maintaining equal cpu cores per container for all machines
        if (getMaximumCpuCoresPerContainer().equals(getAverageCpuCoresPerContainer(0))) {
             
            // we choose the any agent. Doesn't matter which one
            String chosenAgentUid = allocatedCapacityForPu.getAgentUids().iterator().next();
            int containersOnChosenAgent = getNumberOfContainers(chosenAgentUid);
            for (int containersToRemoveFromChosen = containersOnChosenAgent - 1 ; containersToRemoveFromChosen > 0 ; containersToRemoveFromChosen--) {
                Fraction requiredCpuPerContainer = 
                    allocatedCapacityForPu.getAgentCapacity(chosenAgentUid).getCpuCores()
                    .divide(containersOnChosenAgent - containersToRemoveFromChosen);

                // check if we can make all containers have the same cpuPerContainer as the chosen
                boolean success = true;
                Map<String,Integer> numberOfContainersToRemovePerAgent = new HashMap<String,Integer>(); // tentative plan to remove containers
                for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
                    int numberOfContainers = getNumberOfContainers(agentUid);
                    Fraction cpuCores = allocatedCapacityForPu.getAgentCapacity(agentUid).getCpuCores();
                    Fraction requiredNumberOfContainers = cpuCores.divide(requiredCpuPerContainer);
                    if (requiredNumberOfContainers.getDenominator() != 1) {
                        // required number of containers is not an integer
                        success = false;
                        break;
                    }
                    if (requiredNumberOfContainers.getNumerator() > numberOfContainers) {
                        throw new IllegalStateException("Cannot increase denominator and get a bigger number");
                    }
                    numberOfContainersToRemovePerAgent.put(agentUid, numberOfContainers - requiredNumberOfContainers.getNumerator()); 
                }
                
                if (success) {
                    int numberOfContainersToRemove = 0;
                    for (int numberOfContainersToRemoveOnAgent : numberOfContainersToRemovePerAgent.values()) {
                        numberOfContainersToRemove += numberOfContainersToRemoveOnAgent;
                    }
                    if (numberOfContainersToRemove <= excessNumberOfContainers) {
                        // act on plan and remove containers
                        for (String agentUid : numberOfContainersToRemovePerAgent.keySet()) {
                            for (int i = 0 ; i < numberOfContainersToRemovePerAgent.get(agentUid) ; i++) {
                                deallocateCapacityOnMachine(agentUid, oneContainer);
                            }
                        }
                    }
                }
            }

            
        }
    }

    /**
     * Removes excess containers that are not needed. Remaining cpu capacity is spread accross other machines.
     * No machines are removed (never remove the last container on the machine)
     */
    private boolean removeOverMaximumContainers() {
        boolean success = false;
        boolean retry;
        do {
            retry = false;
            for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {               
                
                while (removeExcessContainer(sourceAgentUid, calcOverMaximumMemory(), true)) {
                    retry = true;
                    success = true;
                }
                
                if (retry) {
                    //retry another machine again
                    break;
                }
            }
        } while (retry);
        
        return success;
    }
    
    private long calcOverMaximumMemory() {
        return allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() - maxMemoryCapacityInMB;
    }

    private boolean removeExcessContainer(String sourceAgentUid, long excessMemory, boolean forceRemove) {
        
        boolean success = false;
        
        
        //remove excess capacity that is on this source machine
        AllocatedCapacity allocatedCapacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid);
        
        int numberOfContainersOnSourceMachine = (int) (allocatedCapacityOnSourceMachine.getMemoryInMB()/containerMemoryCapacityInMB);
        
        
        if (excessMemory > 0 && numberOfContainersOnSourceMachine >= 2){
            
            if (forceRemove) {
                deallocateCapacityOnMachine(sourceAgentUid, new AllocatedCapacity(Fraction.ZERO, containerMemoryCapacityInMB));
                success = true;
            }
            else if (excessMemory >= containerMemoryCapacityInMB) {
        
                Fraction goalCpuCoresPerContainerAfterRemovingOneContainer = getMaximumCpuCoresPerContainer();
                Fraction cpuCoresToLeaveOnSourceMachine = goalCpuCoresPerContainerAfterRemovingOneContainer.multiply(numberOfContainersOnSourceMachine-1);
                Fraction cpuCoresToRelocate =  allocatedCapacityOnSourceMachine.getCpuCores().subtract(cpuCoresToLeaveOnSourceMachine);
                if (cpuCoresToRelocate.compareTo(Fraction.ZERO)<0) {
                    cpuCoresToRelocate = Fraction.ZERO;
                }
                if (relocateCapacityToOtherMachines(sourceAgentUid, new AllocatedCapacity(cpuCoresToRelocate, 0))) {
        
                    //deallocate remaining excess container memory from machine
                    deallocateCapacityOnMachine(sourceAgentUid, new AllocatedCapacity(Fraction.ZERO, containerMemoryCapacityInMB));
                    success = true;
                }
            
            }    
            
        }
        
        return success;
    }
    


    private boolean removeExcessMachines(AllocatedCapacity goalCapacity) {
        boolean success = false;
        if (allocatedCapacityForPu.getAgentUids().size() > minimumNumberOfMachines) {
        
            boolean retry;
            do {
                retry = false;
                final AllocatedCapacity excessCapacity = 
                    allocatedCapacityForPu.getTotalAllocatedCapacity().subtractOrZero(goalCapacity);
                
                for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {
                    if (removeExcessMachineStep(sourceAgentUid, excessCapacity)) {
                        //retry another machine again
                        retry = true;
                        success = true;
                        break;
                    }
                }
            } while (retry);
        }
        return success;
    }

    private boolean removeExcessMachineStep(String sourceAgentUid, AllocatedCapacity excessCapacity) {
        
        boolean retry = false;
        AllocatedCapacity allocatedCapacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid);
        
        AllocatedCapacity remainingCapacityToRelocate = 
            allocatedCapacityOnSourceMachine.subtractOrZero(excessCapacity); 
        
        //check that we can relocate all non excess containers to other machines
        if (relocateCapacityToOtherMachines(sourceAgentUid, remainingCapacityToRelocate)) {

            AllocatedCapacity remaminingCapacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid);
            
            if (!excessCapacity.satisfies(remaminingCapacityOnSourceMachine)) {
                throw new IllegalStateException(
                        "Cannot remove machine since it has " + remaminingCapacityOnSourceMachine + " "+
                        "and excess capacity is " + excessCapacity);
            }
            
            //deallocate remaining excess capacity on source machine
            deallocateCapacityOnMachine(
                    sourceAgentUid, 
                    remaminingCapacityOnSourceMachine);
            
            retry = true;
        }
        return retry;
    }

/**
 * Relocates the specified capacity from the specified machine to other machines that have unallocated space.
 * Empty machines ( that do not have any allocated capacity) are not considered as valid targets
 * @return true if relocation completed successfully, 
 * false if not enough capacity found on target containers and no capacity was relocated at all.
 */
    private boolean relocateCapacityToOtherMachines(String sourceAgentUid, AllocatedCapacity remainingCapacityToRelocate) {
        boolean retry = false;
        
        Map<String,AllocatedCapacity> capacityToRelocatePerTargetMachine = new HashMap<String,AllocatedCapacity>();
        for (String targetAgentUid : allocatedCapacityForPu.getAgentUids()) {
            if (remainingCapacityToRelocate.equalsZero()) {
                break;
            }
            if (targetAgentUid.equals(sourceAgentUid)) {
                continue;
            }
            // remember to relocate capacity from source to target
            AllocatedCapacity unallocatedCapacityOnTarget = unallocatedCapacity.getAgentCapacityOrZero(targetAgentUid);
            AllocatedCapacity capacityToRelocate = lowestCommonGoal(unallocatedCapacityOnTarget, remainingCapacityToRelocate);
            capacityToRelocate = capacityToRelocate.subtract(new AllocatedCapacity(Fraction.ZERO,capacityToRelocate.getMemoryInMB() % containerMemoryCapacityInMB));
            if (!capacityToRelocate.equalsZero()) {
                remainingCapacityToRelocate = remainingCapacityToRelocate.subtract(capacityToRelocate);
                capacityToRelocatePerTargetMachine.put(targetAgentUid, capacityToRelocate);
            }
        }
        
        if (remainingCapacityToRelocate.equalsZero()) {
            
            // relocate memory and cpu from source to all targets
            for (String targetAgentUid : capacityToRelocatePerTargetMachine.keySet()) { 
                AllocatedCapacity capacityToRelocate = capacityToRelocatePerTargetMachine.get(targetAgentUid);
                deallocateCapacityOnMachine(sourceAgentUid, capacityToRelocate);
                allocateCapacityOnMachine(  targetAgentUid, capacityToRelocate);
            }
            
            // call this method again. try removing another machine
            retry = true;
        }
        return retry;
    }

    private void allocateNewCapacityForPu(AllocatedCapacity goalCapacity, boolean startExtraContainersToSatisfyCpuShortage) {
        
        if (goalCapacity.getMemoryInMB() % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("goalCapacity memory (" + goalCapacity.getMemoryInMB() + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
        }
        
        // for high availability purposes a failure of one machine should not take down more than half of the cluster (assuming minMachines=2)
        long maxMemoryPerMachine = (long) Math.ceil(goalCapacity.getMemoryInMB()/(1.0*minimumNumberOfMachines));
        
        for (String agentUid : unallocatedCapacity.getAgentUids()) {
            
            long memoryShortage =  goalCapacity.getMemoryInMB() - allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB();
            if (memoryShortage < 0) {
                if (!startExtraContainersToSatisfyCpuShortage) {
                throw new IllegalStateException ("memoryShortage cannot be negative");
               }
               else {
                   memoryShortage = 0;
               }
            }
            Fraction cpuCoresShortage = goalCapacity.getCpuCores().subtract(allocatedCapacityForPu.getTotalAllocatedCapacity().getCpuCores());
            if (cpuCoresShortage.compareTo(Fraction.ZERO) < 0) {
                throw new IllegalStateException("cpuCoresShortage cannot be negative");
            }
            // allocate memory and cpu. doesn't matter on which machine
            if (memoryShortage == 0 && cpuCoresShortage.compareTo(Fraction.ZERO) == 0) {
                break;   
            }
            
            long unallocatedMemoryOnMachine = unallocatedCapacity.getAgentCapacity(agentUid).getMemoryInMB();
            long allocatedMemoryForPuOnMachine = allocatedCapacityForPu.getAgentCapacityOrZero(agentUid).getMemoryInMB();
            if (maxMemoryPerMachine < allocatedMemoryForPuOnMachine) {
                throw new IllegalStateException("allocatedMemoryForPuOnMachine is more than maxMemoryPerMachine");
            }
            
            // calculate memory to allocate on machine based on constraints (such as available free memory, max memory per machine, container Xmx)
            long memoryToAllocateOnMachine = 
                min(memoryShortage, 
                    unallocatedMemoryOnMachine,
                    maxMemoryPerMachine - allocatedMemoryForPuOnMachine);
            memoryToAllocateOnMachine = memoryToAllocateOnMachine - (memoryToAllocateOnMachine % containerMemoryCapacityInMB); 
            
            // calculate cpu to allocate on machine based on constraints (available free cpu)
            Fraction unallocatedCpuOnMachine = unallocatedCapacity.getAgentCapacity(agentUid).getCpuCores();
            Fraction cpuToAllocateOnMachine = min(cpuCoresShortage, unallocatedCpuOnMachine);
            
            // check if trying to allocate cpu without memory
            if (cpuToAllocateOnMachine.compareTo(Fraction.ZERO) > 0 &&
                memoryToAllocateOnMachine + allocatedMemoryForPuOnMachine == 0) { 
                
                if (    //if not enough free memory to allocate container
                        unallocatedMemoryOnMachine < containerMemoryCapacityInMB || 
                        
                        //or if too much already allocated on machine for this pu
                        maxMemoryPerMachine - allocatedMemoryForPuOnMachine < containerMemoryCapacityInMB ||
                        
                        //or if cannot start a container on a new machine
                        !startExtraContainersToSatisfyCpuShortage ||
                        
                        //or if pu is already stretched out to max number of machines
                        allocatedCapacityForPu.getAgentUids().size() >= maxMemoryCapacityInMB/containerMemoryCapacityInMB) {
                    
                    // cancel cpu allocation request
                    cpuToAllocateOnMachine = Fraction.ZERO;
                }
                else {
                    // allocate memory
                    memoryToAllocateOnMachine = containerMemoryCapacityInMB;
                }
            }
            
            AllocatedCapacity capacityToAllocateOnAgent = new AllocatedCapacity(cpuToAllocateOnMachine, memoryToAllocateOnMachine);
            if (!capacityToAllocateOnAgent.equalsZero()) {
               allocateCapacityOnMachine(agentUid, capacityToAllocateOnAgent);
            }
        }
    }

    /**
     * Rebalancing algorithm:
     * 
     * Calculate optimal number of containers per machine and compensate shortage with excess.
     * Calculate optimal number of cpu cores per container and compensate shortage with excess.
     *  
     * Rebalancing Scope:
     * max total memory on all machines: algorithm does not add containers, just moves them
     * memory per machine above total/minNumberOfMachines (due to high availability failover concerns): 
     *          optimization goal is more strict then this constraint because optimalMemory = totalMemory/numberOfMachines < totalMemory/minNumberOfMachines 
     * cpu per container too big: this is part of the optimization goals. 
     * @return 
     *
     */
    boolean rebalanceExistingContainers() {
        
        boolean success = false;
        
        if (rebalanceMemory()) {
            success = true;
        }
        
        if (rebalanceCpuCores()) {
            success = true;
        }
        
        return success;
        
    }

    private boolean rebalanceMemory() {
        
        boolean success = false;
        
        long allocatedMemory = allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB();
        
        int numberOfContainers = (int) (allocatedMemory / containerMemoryCapacityInMB);
        int numberOfMachines = allocatedCapacityForPu.add(unallocatedCapacity).getAgentUids().size();
        
        int minNumberOfContainersPerMachine = (int) Math.floor(1.0*numberOfContainers/numberOfMachines);
        int maxNumberOfContainersPerMachine = (int) Math.ceil(1.0*numberOfContainers/numberOfMachines);
        
        long minMemoryPerMachine = minNumberOfContainersPerMachine * containerMemoryCapacityInMB;
        long maxMemoryPerMachine = maxNumberOfContainersPerMachine * containerMemoryCapacityInMB;
        
        for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {
            if (relocateMemoryFromSourceMachine(minMemoryPerMachine, maxMemoryPerMachine, sourceAgentUid)) {
                success = true;
            }
        }
        
        // it is possible for one of the source machines still to be above the maximum. Hope to rebalance it anyway.
        for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {
            if (relocateMemoryFromSourceMachine(maxMemoryPerMachine, maxMemoryPerMachine, sourceAgentUid)) {
                success = true;
            }
        }
        
        return success;
    }

    private boolean rebalanceCpuCores() {
        
        boolean success = false;
        Fraction goalCpuCoresPerContainer = getAverageCpuCoresPerContainer(0);
        if (!goalCpuCoresPerContainer.equals(Fraction.ZERO)) {
            for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {
                if (relocateCpuFromSourceMachine(goalCpuCoresPerContainer, sourceAgentUid)) {
                    success = true;
                }
            }
        }
        
        return success;
    }

    /**
     * Get the most hard working container
     */
    private Fraction getMaximumCpuCoresPerContainer() {
        
        Fraction maxCpuCoresPerContainer = Fraction.ZERO;
        for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
            final Fraction cpuCoresPerContainer = getCpuCoresPerContainer(agentUid,0);
            if (cpuCoresPerContainer.compareTo(maxCpuCoresPerContainer) > 0) {
                maxCpuCoresPerContainer = cpuCoresPerContainer;
            }
        }
        
        return maxCpuCoresPerContainer;
    }

    private Fraction getCpuCoresPerContainer(String agentUid, int numberOfContainerToRemove) {
        final int numberOfContainers = getNumberOfContainers(agentUid) - numberOfContainerToRemove;
        final Fraction allocatedCpuCores = allocatedCapacityForPu.getAgentCapacity(agentUid).getCpuCores();
        final Fraction cpuCoresPerContainer = allocatedCpuCores.divide(numberOfContainers);
        return cpuCoresPerContainer;
    }

    private int getNumberOfContainers(String agentUid) {
        final long allocatedMemory = allocatedCapacityForPu.getAgentCapacity(agentUid).getMemoryInMB();
        final int numberOfContainers = (int) (allocatedMemory / containerMemoryCapacityInMB);
        return numberOfContainers;
    }

    
    private Fraction getAverageCpuCoresPerContainer(int numberOfContainerToRemove) {
        long allocatedMemory = allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB();
        
        int numberOfContainers = (int) (allocatedMemory / containerMemoryCapacityInMB) - numberOfContainerToRemove;
        Fraction allocatedCpuCores = allocatedCapacityForPu.getTotalAllocatedCapacity().getCpuCores();
        Fraction goalCpuCoresPerContainer = allocatedCpuCores.divide(numberOfContainers);
        return goalCpuCoresPerContainer;
    }

    private boolean relocateMemoryFromSourceMachine(long minMemoryForTargetMachine, long maxMemoryPerSourceMachine, String sourceAgentUid) {
        boolean success = false;
        for (String targetAgentUid : unallocatedCapacity.getAgentUids()) {
        
            if (allocatedCapacityForPu.getAgentCapacityOrZero(targetAgentUid).equalsZero()) {
                // this machine is empty. Don't start a container on a new machine.
                continue;
            }
            
            long memoryOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid).getMemoryInMB();
            long memoryToRelocateFromSource = memoryOnSourceMachine - maxMemoryPerSourceMachine;
            
            if (memoryToRelocateFromSource <= 0) {
                break;
            }
            
            if (sourceAgentUid.equals(targetAgentUid)) {
                // cannot relocate to itself
                continue;
            }
            
            long memoryOnTargetMachine = allocatedCapacityForPu.getAgentCapacityOrZero(targetAgentUid).getMemoryInMB();
            
            if (memoryOnTargetMachine + containerMemoryCapacityInMB > memoryOnSourceMachine -containerMemoryCapacityInMB) {
                // the target machine after the relocation will have more memory than the source machine.
                // therefore relocation has no real effect.
                continue;
            }
                            
            long memoryToRelocateToTarget = 
                min( memoryToRelocateFromSource, 
                     minMemoryForTargetMachine - memoryOnTargetMachine,
                     unallocatedCapacity.getAgentCapacity(targetAgentUid).getMemoryInMB());
            
            if (memoryToRelocateToTarget <= 0) { 
                // the target machine has the maximum number of containers, 
                // or not enough unallocated space
                // not good to relocating containers from source to target
                    continue;
            }
            
            deallocateCapacityOnMachine(sourceAgentUid, new AllocatedCapacity(Fraction.ZERO, memoryToRelocateToTarget));
            allocateCapacityOnMachine(targetAgentUid, new AllocatedCapacity(Fraction.ZERO, memoryToRelocateToTarget));
            success = true;
        }
        
        return success;
    }
    
    private boolean relocateCpuFromSourceMachine(Fraction goalCpuCoresPerContainer, String sourceAgentUid) {
        
        boolean success = false;
        
        for (String targetAgentUid : unallocatedCapacity.getAgentUids()) {
        
            Fraction cpuCoresOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid).getCpuCores();
            long memoryOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid).getMemoryInMB();
            
            int numberOfContainersOnSourceMachine = (int) (memoryOnSourceMachine / containerMemoryCapacityInMB);
            if (numberOfContainersOnSourceMachine == 0) {
                // no containers on this machine to relocate cpu to
                continue;
            }
            Fraction goalCpuCoresOnSourceMachine = goalCpuCoresPerContainer.multiply(numberOfContainersOnSourceMachine);
            Fraction cpuCoresToRelocateFromSource = cpuCoresOnSourceMachine.subtract(goalCpuCoresOnSourceMachine);
            
            if (cpuCoresToRelocateFromSource.compareTo(Fraction.ZERO) <= 0) {
                break;
            }
            
            if (sourceAgentUid.equals(targetAgentUid)) {
                // cannot relocate to itself
                continue;
            }
            
            AllocatedCapacity capacityOnTargetMachine = allocatedCapacityForPu.getAgentCapacityOrZero(targetAgentUid);
            long memoryOnTargetMachine = capacityOnTargetMachine.getMemoryInMB();
            int numberOfContainersOnTargetMachine = (int) (memoryOnTargetMachine / containerMemoryCapacityInMB);
            Fraction goalCpuCoresOnTargetMachine = goalCpuCoresPerContainer.multiply(numberOfContainersOnTargetMachine);
            
            Fraction cpuCoresToRelocateToTarget = 
                min( cpuCoresToRelocateFromSource, 
                     goalCpuCoresOnTargetMachine.subtract(capacityOnTargetMachine.getCpuCores()),
                     unallocatedCapacity.getAgentCapacity(targetAgentUid).getCpuCores());
            
            if (cpuCoresToRelocateToTarget.compareTo(Fraction.ZERO) <= 0) {
                // the target machine has the maximum number of cpu cores
                // or not enough unallocated cpu cores
                // do not relocate cpu cores from source to target
                continue;
            }
            
            deallocateCapacityOnMachine(sourceAgentUid, new AllocatedCapacity(cpuCoresToRelocateToTarget, 0));
            allocateCapacityOnMachine(targetAgentUid, new AllocatedCapacity(cpuCoresToRelocateToTarget,0));
            
            success = true;
        }
        
        return success;
    }

    private Fraction min(Fraction a, Fraction b, Fraction c) {
        return min(min(a,b),c);
    }

    private long min(long a, long b, long c) {
        return Math.min(Math.min(a, b),c);
    }

    private AllocatedCapacity getGoalCapacityScaleOut(AllocatedCapacity capacityToAllocate) {
        
        // calculate total allocate memory and round up to the nearest container
        long totalMemory = allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() + 
            Math.min(capacityToAllocate.getMemoryInMB(),unallocatedCapacity.getTotalAllocatedCapacity().getMemoryInMB());
        long partialContainerMemory = totalMemory % containerMemoryCapacityInMB;
        if (partialContainerMemory > 0) {
            totalMemory += containerMemoryCapacityInMB - partialContainerMemory; 
        }
        
        if (this.maxMemoryCapacityInMB < totalMemory) {
            totalMemory = maxMemoryCapacityInMB;
        }
        
        Fraction totalCpuCores = 
            allocatedCapacityForPu.getTotalAllocatedCapacity().getCpuCores().add(
                    min(capacityToAllocate.getCpuCores(),unallocatedCapacity.getTotalAllocatedCapacity().getCpuCores()));
        
        return new AllocatedCapacity(totalCpuCores,totalMemory);
    }
    
    private AllocatedCapacity getGoalCapacityScaleIn(AllocatedCapacity capacityToDeallocate) {
        
        // calculate total allocate memory and round up to the nearest container
        long totalMemory = allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() - capacityToDeallocate.getMemoryInMB();
                
        long partialContainerMemory = totalMemory % containerMemoryCapacityInMB;
        if (partialContainerMemory > 0) {
            totalMemory += containerMemoryCapacityInMB - partialContainerMemory; 
        }

        long minimumMemoryInMB = minimumNumberOfMachines*containerMemoryCapacityInMB;
        if (totalMemory < minimumMemoryInMB) {
           throw new IllegalArgumentException("cannot deallocate " + capacityToDeallocate.getMemoryInMB() + "MB, since only allocated " + allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() + " and minimum memory is "+minimumMemoryInMB);
        }
        
        Fraction totalCpuCores = allocatedCapacityForPu.getTotalAllocatedCapacity().getCpuCores().subtract(capacityToDeallocate.getCpuCores());
        if (totalCpuCores.compareTo(Fraction.ZERO) < 0) {
            throw new IllegalArgumentException("cannot deallocate " + capacityToDeallocate.getCpuCores() + " cpu cores, since only allocated " + allocatedCapacityForPu.getTotalAllocatedCapacity().getCpuCores());
        }
        
        return new AllocatedCapacity(totalCpuCores,totalMemory);
    }
    
    private void allocateCapacityOnMachine(String agentUid, AllocatedCapacity capacityToAllocateOnAgent) {
        
        if (capacityToAllocateOnAgent.getMemoryInMB() % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("allocatedCapacityOnSourceMachine memory (" + capacityToAllocateOnAgent.getMemoryInMB() + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
        }
        
        // un-deallocate capacity first before allocating
        AllocatedCapacity deallocated = lowestCommonGoal(capacityToAllocateOnAgent,deallocatedCapacityResult.getAgentCapacityOrZero(agentUid));
        if (!deallocated.equalsZero()) {
            deallocatedCapacityResult = deallocatedCapacityResult.subtract(agentUid, deallocated);
            capacityToAllocateOnAgent = capacityToAllocateOnAgent.subtract(deallocated);
        }
        
        // allocate capacity
        if (!capacityToAllocateOnAgent.equalsZero()) {
            allocatedCapacityResult = allocatedCapacityResult.add(agentUid, capacityToAllocateOnAgent);
            unallocatedCapacity = unallocatedCapacity.subtract(agentUid, capacityToAllocateOnAgent);
            allocatedCapacityForPu = allocatedCapacityForPu.add(agentUid, capacityToAllocateOnAgent);
        }
    }
    
    private void deallocateCapacityOnMachine(String agentUid, AllocatedCapacity capacityToDeallocateOnAgent) {
        
        if (capacityToDeallocateOnAgent.getMemoryInMB() % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("allocatedCapacityOnSourceMachine memory (" + capacityToDeallocateOnAgent.getMemoryInMB() + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
        }
        
        // un-allocate capacity first before deallocating
        AllocatedCapacity allocated = lowestCommonGoal(capacityToDeallocateOnAgent,allocatedCapacityResult.getAgentCapacityOrZero(agentUid));
        if (!allocated.equalsZero()) {
            allocatedCapacityResult = allocatedCapacityResult.subtract(agentUid, allocated);
            unallocatedCapacity = unallocatedCapacity.add(agentUid, allocated);
            allocatedCapacityForPu = allocatedCapacityForPu.subtract(agentUid, allocated);
            capacityToDeallocateOnAgent = capacityToDeallocateOnAgent.subtract(allocated);
        }
        
        // deallocate capacity
        if (!capacityToDeallocateOnAgent.equalsZero()) {
            deallocatedCapacityResult = deallocatedCapacityResult.add(agentUid, capacityToDeallocateOnAgent);
            unallocatedCapacity = unallocatedCapacity.add(agentUid, capacityToDeallocateOnAgent);
            allocatedCapacityForPu = allocatedCapacityForPu.subtract(agentUid, capacityToDeallocateOnAgent);
        }
    }
    
    private static AllocatedCapacity lowestCommonGoal(AllocatedCapacity allocatedCapacity1, AllocatedCapacity allocatedCapacity2) {
        return new AllocatedCapacity(
                min(allocatedCapacity1.getCpuCores(),allocatedCapacity2.getCpuCores()), 
                Math.min(allocatedCapacity1.getMemoryInMB(), allocatedCapacity2.getMemoryInMB()));
    }

    private static Fraction min(Fraction fraction1, Fraction fraction2) {
        if (fraction1.compareTo(fraction2) <= 0) {
            return fraction1;
        }
        else {
            return fraction2;
        }
    }

    public void solveNumberOfMachines(int numberOfMachines) {
                
        validate();
        
        //remove all agents that already have a pu allocation, since we are looking only for new machines
        Collection<String> agentUidsOfPu = allocatedCapacityForPu.getAgentUids();
        if (!agentUidsOfPu.isEmpty()) {
            for (String agentUid : unallocatedCapacity.getAgentUids()) {
                if (agentUidsOfPu.contains(agentUid)) {
                    unallocatedCapacity = unallocatedCapacity.subtractAgent(agentUid);
                }
            }
        }
        
    

        while (allocatedCapacityResult.getAgentUids().size() < numberOfMachines) {
            // try allocate only memory on a new machine
            AllocatedCapacity capacityToAllocateOnAgent = new AllocatedCapacity(Fraction.ZERO,containerMemoryCapacityInMB);
            String agentUid = findFreeAgentUid(capacityToAllocateOnAgent);
            
            if (agentUid == null) {
                //allocation failed.
                break;
            }
            
            if (isNewContainerWillBreachMaximumMemory()) {
                removeContainerFromMachineWithMoreThanOneContainerAndMaxUnallocatedMemory();
            }
            
            if (isNewContainerWillBreachMaximumMemory()) {
                break;
            }
        
            if (!deallocatedCapacityResult.getAgentCapacityOrZero(agentUid).equalsZero()) {
                throw new IllegalStateException("Impossible to allocate and deallocate from the same agent " + agentUid);
            }
            allocatedCapacityResult = allocatedCapacityResult.add(agentUid, capacityToAllocateOnAgent);
            allocatedCapacityForPu = allocatedCapacityForPu.add(agentUid, capacityToAllocateOnAgent);
            //leave unallocated capacity for this agent deleted so it wont be allocated
            unallocatedCapacity = unallocatedCapacity.subtractAgent(agentUid);
            
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("BinPackingSolver: number of machines " + numberOfMachines + " allocatedCapacityResult=" + this.getAllocatedCapacityResult().toDetailedString() + " deallocatedCapacityResult="+this.getDeallocatedCapacityResult());
        }

    }

    private void removeContainerFromMachineWithMoreThanOneContainerAndMaxUnallocatedMemory() {
        long maxUnallocatedMemoryInMB = Long.MAX_VALUE;
        String agentUidToRemoveContainer = null;
        
        for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
            long unallocated = unallocatedCapacity.getAgentCapacityOrZero(agentUid).getMemoryInMB();
            long allocatedByPu = allocatedCapacityForPu.getAgentCapacity(agentUid).getMemoryInMB();
            if (allocatedByPu >= containerMemoryCapacityInMB*2 && maxUnallocatedMemoryInMB < unallocated) {
                maxUnallocatedMemoryInMB = unallocated;
                agentUidToRemoveContainer = agentUid;
            }
        }
        
        if (agentUidToRemoveContainer != null) {
            AllocatedCapacity capacityToRemove = new AllocatedCapacity(Fraction.ZERO, containerMemoryCapacityInMB);
            if (!allocatedCapacityResult.getAgentCapacity(agentUidToRemoveContainer).equalsZero()) {
                throw new IllegalStateException("Impossible to allocate and deallocate from the same agent " + agentUidToRemoveContainer);
            }
            deallocatedCapacityResult = deallocatedCapacityResult.add(agentUidToRemoveContainer, capacityToRemove);
            allocatedCapacityForPu = allocatedCapacityForPu.subtract(agentUidToRemoveContainer, capacityToRemove);
            //leave unallocated capacity for this agent deleted so it wont be allocated
        }
        
    }

    private boolean isNewContainerWillBreachMaximumMemory() {
        return allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() + containerMemoryCapacityInMB > maxMemoryCapacityInMB;
    }

    private void validate() {
        if (containerMemoryCapacityInMB == 0) {
            throw new IllegalArgumentException("containerMemoryCapacityInMB");
        }
        
        if (maxMemoryCapacityInMB == 0) {
            throw new IllegalArgumentException("maxMemoryCapacityInMB");
        }
        
        if (maxMemoryCapacityInMB % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("max memory capacity must divide by " + containerMemoryCapacityInMB);
        }
        
        if (allocatedCapacityForPu.getTotalAllocatedCapacity().getMemoryInMB() > maxMemoryCapacityInMB) {
            throw new IllegalArgumentException("total PU allocated capacity exceeds the specified max number of containers");
        }
        
       for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
           AllocatedCapacity agentCapacity = allocatedCapacityForPu.getAgentCapacity(agentUid);
           if (agentCapacity.getMemoryInMB() % containerMemoryCapacityInMB != 0) {
               throw new IllegalArgumentException("agentCapacity memory (" + agentCapacity.getMemoryInMB() + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
           }
       }
        
        if (minimumNumberOfMachines == 0) {
            throw new IllegalArgumentException("minimumNumberOfMachines");
        }
        

        if (logger.isDebugEnabled()) {
            logger.debug("BinPackingSolver: containerMemoryCapacityInMB=" + containerMemoryCapacityInMB+ " maxMemoryCapacityInMB="+maxMemoryCapacityInMB + " unallocatedCapacity="+unallocatedCapacity.toDetailedString() + " allocatedCapacityForPu="+allocatedCapacityForPu.toDetailedString() + " minimumNumberOfMachines="+minimumNumberOfMachines);
        }
        
    }
    
    private String findFreeAgentUid(AllocatedCapacity capacityToAllocateOnAgent) {
        
        logger.debug("Looking for an agent to allocate " + capacityToAllocateOnAgent);
        
        long minUnallocatedMemoryInMB = Long.MAX_VALUE;
        String chosenAgentUid = null;
        
        for (String agentUid : unallocatedCapacity.getAgentUids()) {
            
            if (capacityToAllocateOnAgent.isMemoryEqualsZero() &&
                (!allocatedCapacityForPu.getAgentUids().contains(agentUid) ||
                 allocatedCapacityForPu.getAgentCapacityOrZero(agentUid).isMemoryEqualsZero())) {
                    logger.debug("Cannot allocate on agent " + agentUid + " without memory, since it does not already have any memory allocated");
                    continue;
            }
            
            AllocatedCapacity unallocatedCapacityOnAgent = unallocatedCapacity.getAgentCapacity(agentUid);
            
            if (!unallocatedCapacityOnAgent.satisfies(capacityToAllocateOnAgent)) {
                logger.debug("Cannot allocate on agent " + agentUid + " since it does not have enough unallocated capacity");
                continue;
            }
            
            // The best fit agent, has the least unallocated memory. 
            // This is to maximize the size of continuous unallocated memory blocks in other agents (bin-packing Tetris heuristics)
            if (minUnallocatedMemoryInMB > unallocatedCapacityOnAgent.getMemoryInMB()) {
                
                chosenAgentUid = agentUid;
                minUnallocatedMemoryInMB = unallocatedCapacityOnAgent.getMemoryInMB();
            }
        }
        if (chosenAgentUid != null) {
            logger.debug("Choosed agent " + chosenAgentUid + " to allocate " + capacityToAllocateOnAgent + " since it has the least ammount of unallocated memory " + minUnallocatedMemoryInMB);
        }
        else {
            logger.debug("Cannot find agent that can allocate " + capacityToAllocateOnAgent);
        }
        return chosenAgentUid;
    }

    public AggregatedAllocatedCapacity getAllocatedCapacityResult() {
        return allocatedCapacityResult;
    }
    
    public AggregatedAllocatedCapacity getDeallocatedCapacityResult() {
        return deallocatedCapacityResult;
    }

    public AggregatedAllocatedCapacity getAllocatedCapacityForPu() {
        return allocatedCapacityForPu;
    }
    
    public AggregatedAllocatedCapacity getUnallocatedCapacity() {
        return unallocatedCapacity;
    }

    /**
     * cleans AllocatedResult and DeallocatedResult
     * Used for unit testing
     */
    public void reset() {
        this.deallocatedCapacityResult = new AggregatedAllocatedCapacity();
        this.allocatedCapacityResult = new AggregatedAllocatedCapacity();
    }
}
