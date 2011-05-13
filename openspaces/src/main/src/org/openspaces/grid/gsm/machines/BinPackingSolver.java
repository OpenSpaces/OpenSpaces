package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirementType;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;

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
    private ClusterCapacityRequirements unallocatedCapacity;
    private ClusterCapacityRequirements allocatedCapacityForPu;
    private ClusterCapacityRequirements allocatedCapacityResult;
    private ClusterCapacityRequirements deallocatedCapacityResult;
    private String debugTrace = "";
    
    private long maxMemoryCapacityInMB;
    private int minimumNumberOfMachines;
    private HashMap<String, Integer> agentPriority;

    public BinPackingSolver() {
        debugTrace ="";
        agentPriority = new HashMap<String,Integer>();
        allocatedCapacityResult = new ClusterCapacityRequirements();
        deallocatedCapacityResult = new ClusterCapacityRequirements();
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
    public void setUnallocatedCapacity(ClusterCapacityRequirements unallocatedCapacity) {
        this.unallocatedCapacity = unallocatedCapacity;
    }
    
    /**
     * The higher the priority the less likely for the machine to be scaled in
     * @param agentPriority - a map between agent UID and its priority
     */
    public void setAgentAllocationPriority(Map<String, Integer> agentPriority) {
        this.agentPriority = new HashMap<String,Integer>(agentPriority);
    }
    
    /**
     * Sets the currently allocated capacity of PU
     */
    public void setAllocatedCapacityForPu(ClusterCapacityRequirements allocatedCapacityForPu) {
        this.allocatedCapacityForPu = allocatedCapacityForPu;
    }


    /**
     * Sets the maximum total memory that can be allocated for PU
     */
    public void setMaxAllocatedMemoryCapacityOfPuInMB(long maxMemoryInMB) {
        this.maxMemoryCapacityInMB = maxMemoryInMB;
    }


    public void solveManualCapacityScaleIn(CapacityRequirements capacityToDeallocate) {
        boolean success = false;
        try {
            debugTrace = "BinPackingSolver: manual capacity scale in " + capacityToDeallocate;
            validateInput();
        
            unallocatedCapacity = roundFloorMemoryToContainerMemory(unallocatedCapacity);
            CapacityRequirements goalCapacity = getGoalCapacityScaleIn(capacityToDeallocate); // allocatedForPu - capacityToAllocate
            
            // try to remove complete machines then try to remove part of machines (containers)
            removeExcessMachines(goalCapacity);
            removeExcessContainers(goalCapacity);   
            
            // rebalance containers between machines (without adding/removing containers)
            rebalanceExistingContainers();
            
            debugTrace += 
                " allocatedCapacityResult=" + this.getAllocatedCapacityResult().toDetailedString() +
                " deallocatedCapacityResult="+this.getDeallocatedCapacityResult().toDetailedString();
            
            validateResult();
            success = true;
        }
        finally {
            if (logger.isDebugEnabled()) {
                logger.debug(debugTrace);
            }
            else if (!success && logger.isInfoEnabled()) {
                logger.info(debugTrace);
            }
        }
    }
    
    private void validateResult() {
        
        if (getMemoryInMB(allocatedCapacityForPu) > maxMemoryCapacityInMB) {
            throw new IllegalStateException("Allocated memory is more than maximum " + maxMemoryCapacityInMB + "MB: " + allocatedCapacityForPu.toDetailedString());
        }
        
        if (allocatedCapacityForPu.getAgentUids().size() >= minimumNumberOfMachines) {
            long totalMemory = getMemoryInMB(allocatedCapacityForPu);
            long maxMemoryPerMachine = (long) Math.ceil(totalMemory/(1.0*minimumNumberOfMachines));
        
            for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
                long agentMemory = getMemoryInMB(allocatedCapacityForPu.getAgentCapacity(agentUid));
                if (agentMemory > maxMemoryPerMachine) {
                    throw new IllegalStateException("Agent " + agentUid + " bin packing solver results in " + agentMemory
                            + "MB which is more than the maximum allowed "+maxMemoryPerMachine+"MB.");
                }
            }
        }
    }

    public void solveManualCapacityScaleOut(CapacityRequirements capacityToAllocate) {
        boolean success = false;
        try {
            debugTrace = "BinPackingSolver: manual capacity scale out " + capacityToAllocate;
            validateInput();
                    
            unallocatedCapacity = roundFloorMemoryToContainerMemory(unallocatedCapacity);
            CapacityRequirements requestedGoalCapacity = getGoalCapacityScaleOut(capacityToAllocate); // allocatedForPu + capacityToAllocate
            CapacityRequirements realisticGoalCapacity = requestedGoalCapacity; // that we can actually allocate, gets updated as we go along
            
            // do whatever we can to reach the target capacity.
            boolean startExcessContainersToSatisfyNonMemoryShortage = false;
            // Allocates the memory capacity (number of containers)
            allocateNewMemoryCapacityForPu(getMemoryInMB(realisticGoalCapacity));
            
            // Allocates the non-memory capacity without adding memory (containers) above goal
            allocateNewCapacityForPu(realisticGoalCapacity, startExcessContainersToSatisfyNonMemoryShortage);
            
            if (!allocatedCapacityForPu.getTotalAllocatedCapacity().greaterOrEquals(realisticGoalCapacity)) {
                
                if (getMemoryInMB(realisticGoalCapacity) == getMemoryInMB(allocatedCapacityForPu)) {
                    
                    //try again, this time allocate memory (start new containers) more than memory goal 
                    //to satisfy non-memory goal.
                    startExcessContainersToSatisfyNonMemoryShortage = true;
                    allocateNewCapacityForPu(requestedGoalCapacity, startExcessContainersToSatisfyNonMemoryShortage);
                }
        
                // accept the fact that we might not have enough capacity to reach the target
                // rebalance what we have now
                realisticGoalCapacity = realisticGoalCapacity.min(allocatedCapacityForPu.getTotalAllocatedCapacity());
            }
            
            // try to remove complete machines then try to remove part of machines (containers)
            removeExcessMachines(realisticGoalCapacity);
            removeExcessContainers(realisticGoalCapacity);
            removeOverMaximumContainers(realisticGoalCapacity);    
            
            // rebalance containers between machines (without adding/removing containers)
            rebalanceExistingContainers();
            
            debugTrace += 
                " allocatedCapacityResult=" + this.getAllocatedCapacityResult().toDetailedString() + 
                " deallocatedCapacityResult="+this.getDeallocatedCapacityResult().toDetailedString();
            
            if (allocatedCapacityForPu.getTotalAllocatedCapacity().equals(requestedGoalCapacity)) {
                // we completed the allocation of all the capacity requested (no lack of resources)
                // make sure it does not violate basic allocation restrictions
                validateResult();
            }
            success = true;
        }
        finally {
            if (logger.isDebugEnabled()) {
                logger.debug(debugTrace);
            }
            else if (!success && logger.isInfoEnabled()) {
                logger.info(debugTrace);
            }
        }
    }

    /**
     * Removes excess containers that are not needed. Remaining non-memory capacity is spread across other machines.
     * No machines are removed (never remove the last container on the machine)
     */
    private void removeExcessContainers(CapacityRequirements goalCapacity) {
        
        if (!allocatedCapacityForPu.getTotalAllocatedCapacity().greaterOrEquals(goalCapacity)) {
            throw new IllegalStateException("Removing containers assumes that the goal " + goalCapacity +" is already satisfied.");
        }
        
        CapacityRequirements oneContainer = new CapacityRequirements(new MemoryCapacityRequirement(containerMemoryCapacityInMB));
        CapacityRequirements maxNonMemoryCapacityPerContainer = getMaximumNonMemoryCapacityPerContainer();
        boolean retry;
        do {
            retry = false;
            for (String agentUid : allocatedCapacityForPu.getAgentUids()) {               
        
                if (!allocatedCapacityForPu.getTotalAllocatedCapacity().subtractOrZero(oneContainer).greaterOrEquals(goalCapacity)) {
                    // cannot remove any container without breaching goal
                    break;
                }
                
                if (getNumberOfContainers(agentUid) <= 1) {
                    // cannot remove container without removing the machine
                    continue;
                }
            
                CapacityRequirements nonMemoryCapacityPerContainerAfterRemovingContainer = getAverageNonMemoryCapacityPerContainer(agentUid,1);
                if (nonMemoryCapacityPerContainerAfterRemovingContainer.greaterThan(maxNonMemoryCapacityPerContainer)) {
                    // cannot remove container without introducing a container that works harder than the max
                    continue;
                }
                        
                // remove container
                deallocateCapacityOnMachine(agentUid, oneContainer);
                retry = true;
            }
        } while (retry);
        
        int excessNumberOfContainers = 
            (int) Math.floor(1.0*(getMemoryInMB(allocatedCapacityForPu) - getMemoryInMB(goalCapacity)) / containerMemoryCapacityInMB);
        
        if (excessNumberOfContainers < 1) {
            // cannot remove any container without breaching goal
            return;
        }
        
        // check special case in which non-memory capacity is equal for all containers. 
        if (getMaximumNonMemoryCapacityPerContainer().equals(getAverageNonMemoryCapacityPerContainer())) {
            
            // Try reduce the number of containers at the same time for all machines while maintaining equal non-memory per container for all machines
            
            // First, choose the any agent. Doesn't matter which one
            String chosenAgentUid = allocatedCapacityForPu.getAgentUids().iterator().next();
            int containersOnChosenAgent = getNumberOfContainers(chosenAgentUid);
            for (int containersToRemoveFromChosen = containersOnChosenAgent - 1 ; containersToRemoveFromChosen > 0 ; containersToRemoveFromChosen--) {
                CapacityRequirements agentCapacity = allocatedCapacityForPu.getAgentCapacity(chosenAgentUid);
                CapacityRequirements requiredNonMemoryCapacityPerContainer = 
                    agentCapacity
                    .set(new MemoryCapacityRequirement())
                    .divide(containersOnChosenAgent - containersToRemoveFromChosen);

                // check if we can make all containers have the same capacity as the chosen
                boolean success = true;
                Map<String,Integer> numberOfContainersToRemovePerAgent = new HashMap<String,Integer>(); // tentative plan to remove containers
                for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
                    int numberOfContainers = getNumberOfContainers(agentUid);
                    CapacityRequirements nonMemoryCapacity = 
                        allocatedCapacityForPu.getAgentCapacity(agentUid)
                        .set(new MemoryCapacityRequirement());
                    int requiredNumberOfContainers = nonMemoryCapacity.divideExactly(requiredNonMemoryCapacityPerContainer);
                    if (requiredNumberOfContainers == -1) {
                        // required number of containers is not an integer
                        success = false;
                        break;
                    }
                    if (requiredNumberOfContainers > numberOfContainers) {
                        throw new IllegalStateException("Cannot add containers requiredNumberOfContainers="+requiredNumberOfContainers +" numberOfContainers=" + numberOfContainers);
                    }
                    numberOfContainersToRemovePerAgent.put(agentUid, numberOfContainers - requiredNumberOfContainers); 
                }
                
                if (success) {
                    int numberOfContainersToRemove = 0;
                    for (int numberOfContainersToRemoveOnAgent : numberOfContainersToRemovePerAgent.values()) {
                        numberOfContainersToRemove += numberOfContainersToRemoveOnAgent;
                    }
                    if (numberOfContainersToRemove <= excessNumberOfContainers) {
                        // act on plan and remove containers
                        for (Entry<String, Integer> containersToRemoveForAgent : numberOfContainersToRemovePerAgent.entrySet()) {
                            for (int i = 0 ; i < containersToRemoveForAgent.getValue() ; i++) {
                                deallocateCapacityOnMachine(containersToRemoveForAgent.getKey(), oneContainer);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes excess containers that are not needed. Remaining cpu capacity is spread across other machines.
     * No machines are removed (never remove the last container on the machine)
     */
    private boolean removeOverMaximumContainers(CapacityRequirements goalCapacity) {
        boolean success = false;
        boolean retry;
        do {
            retry = false;
            for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {               
                
                while (removeExcessContainer(sourceAgentUid, calcOverMaximumMemory(), goalCapacity)) {
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
        return getMemoryInMB(allocatedCapacityForPu) - maxMemoryCapacityInMB;
    }

    private boolean removeExcessContainer(String sourceAgentUid, long excessMemory, CapacityRequirements goalCapacity) {

        boolean success = false;
        
        //remove excess capacity that is on this source machine
        CapacityRequirements allocatedCapacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid);
        
        int numberOfContainersOnSourceMachine = (int) (getMemoryInMB(allocatedCapacityOnSourceMachine)/containerMemoryCapacityInMB);
        
        if (excessMemory > 0 && numberOfContainersOnSourceMachine >= 2){
            deallocateCapacityOnMachine(sourceAgentUid, new MemoryCapacityRequirement(containerMemoryCapacityInMB));
            success = true;
        }
        
        return success;
    }
    


    private boolean removeExcessMachines(CapacityRequirements goalCapacity) {
        boolean success = false;
        if (allocatedCapacityForPu.getAgentUids().size() > minimumNumberOfMachines) {
        
            boolean retry;
            do {
                retry = false;
                final CapacityRequirements excessCapacity = 
                    allocatedCapacityForPu.getTotalAllocatedCapacity().subtractOrZero(goalCapacity);
                
                // try to evacuate first, machines that are in lower priority
                // sort agents by priority
                List<String> sortedAgentUids = new ArrayList<String>(allocatedCapacityForPu.getAgentUids());
                Collections.sort(sortedAgentUids, new Comparator<String>() {

                    public int compare(String agentUid1, String agentUid2) {
                        return getAgentPriority(agentUid1) - getAgentPriority(agentUid2);
                    }

                    private int getAgentPriority(String agentUid) {
                        int priority = 0;
                        if (agentPriority.containsKey(agentUid)) {
                            priority = agentPriority.get(agentUid);
                        }
                        return priority;
                    }
                });
                
                for (String sourceAgentUid : sortedAgentUids) {
                    if (removeExcessMachineStep(sourceAgentUid, excessCapacity, goalCapacity)) {
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

    private boolean removeExcessMachineStep(String sourceAgentUid, CapacityRequirements excessCapacity, CapacityRequirements goalCapacity) {
        
        boolean retry = false;
        CapacityRequirements allocatedCapacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid);
        
        CapacityRequirements remainingCapacityToRelocate = 
            allocatedCapacityOnSourceMachine.subtractOrZero(excessCapacity); 
        
        //check that we can relocate all non excess containers to other machines
        if (relocateCapacityToOtherMachines(sourceAgentUid, remainingCapacityToRelocate, goalCapacity)) {

            CapacityRequirements remaminingCapacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacityOrZero(sourceAgentUid);
            
            if (!excessCapacity.greaterOrEquals(remaminingCapacityOnSourceMachine)) {
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
    private boolean relocateCapacityToOtherMachines(String sourceAgentUid, CapacityRequirements remainingCapacityToRelocate, CapacityRequirements goalCapacity) {
        boolean retry = false;
        
        Map<String,CapacityRequirements> capacityToRelocatePerTargetMachine = new HashMap<String,CapacityRequirements>();
        for (String targetAgentUid : allocatedCapacityForPu.getAgentUids()) {
            if (remainingCapacityToRelocate.equalsZero()) {
                break;
            }
            if (targetAgentUid.equals(sourceAgentUid)) {
                continue;
            }
            // calc how much can be relocated from source to target based on 
            // unallocated capacity and maxMemoryPerMachine restriction on target.
            CapacityRequirements unallocatedCapacityOnTarget = unallocatedCapacity.getAgentCapacityOrZero(targetAgentUid);
            long maxMemoryPerMachine = (long) Math.ceil(getMemoryInMB(goalCapacity)/(1.0*minimumNumberOfMachines));
            long maxUnallocatedMemoryOnTarget = 
                Math.max(
                        0,
                        maxMemoryPerMachine - getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(targetAgentUid)));
            if (getMemoryInMB(unallocatedCapacityOnTarget) > maxUnallocatedMemoryOnTarget) {
                unallocatedCapacityOnTarget = 
                    unallocatedCapacityOnTarget.max(new MemoryCapacityRequirement(maxUnallocatedMemoryOnTarget));
            }
            
            // remember to relocate capacity from source to target
            CapacityRequirements capacityToRelocate = unallocatedCapacityOnTarget.min(remainingCapacityToRelocate);
            // round memory to nearest containerMemoryCapacityInMB
            capacityToRelocate = roundFloorMemoryToContainerMemory(capacityToRelocate);
            if (!capacityToRelocate.equalsZero()) {
                remainingCapacityToRelocate = remainingCapacityToRelocate.subtract(capacityToRelocate);
                capacityToRelocatePerTargetMachine.put(targetAgentUid, capacityToRelocate);
            }
        }
        
        if (remainingCapacityToRelocate.equalsZero()) {
            
            // relocate memory and cpu from source to all targets
            for (Entry<String, CapacityRequirements> capacityToRelocateForAgent : capacityToRelocatePerTargetMachine.entrySet()) { 
                CapacityRequirements capacity = capacityToRelocateForAgent.getValue();
                String targetAgentUid = capacityToRelocateForAgent.getKey();
                deallocateCapacityOnMachine(sourceAgentUid, capacity);
                allocateCapacityOnMachine(  targetAgentUid, capacity);
            }
            
            // call this method again. try removing another machine
            retry = true;
        }
        return retry;
    }
    
    private ClusterCapacityRequirements roundFloorMemoryToContainerMemory(ClusterCapacityRequirements clusterCapacityRequirements) {
        for (String agent : clusterCapacityRequirements.getAgentUids()) {
            CapacityRequirements agentCapacity = clusterCapacityRequirements.getAgentCapacity(agent);
            CapacityRequirements fixedAgentCapacity = roundFloorMemoryToContainerMemory(agentCapacity);
            if (fixedAgentCapacity != agentCapacity) {
                clusterCapacityRequirements = clusterCapacityRequirements.set(agent,fixedAgentCapacity);
            }
        }
        return clusterCapacityRequirements;
    }

    /**
     * Allocate memory until the specified goal is reached
     */
    private void allocateNewMemoryCapacityForPu(long goalMemoryCapacityInMB) {
        long memoryToAllocateOnMachine;
        
        do {
            memoryToAllocateOnMachine = 0;
            
            //filter agents that have unallocated capacity but not unallocated memory.
            ClusterCapacityRequirements unallocatedMemory = unallocatedCapacity;
            for (String agentUid: unallocatedMemory.getAgentUids()) {
                if (getMemoryInMB(unallocatedMemory.getAgentCapacity(agentUid)) == 0) {
                    unallocatedMemory = unallocatedMemory.subtractAgent(agentUid);
                }
            }

            
           // calculate memory to allocate on machine based on constraints (such as available free memory, max memory per machine, container Xmx)
           long memoryShortage =  goalMemoryCapacityInMB - getMemoryInMB(allocatedCapacityForPu);
            if (memoryShortage < 0) {
                throw new IllegalStateException ("memoryShortage cannot be negative");
            }
            if (memoryShortage == 0 || unallocatedMemory.equalsZero()) {
                // nothing to do
                break;
            }
            if (unallocatedMemory.getAgentUids().size() > 1 &&
                memoryShortage > containerMemoryCapacityInMB) {
            
                // we need to allocate memory on two different machines at the same time
                // otherwise we might violate the limitation of one machine having more than 50% of the total memory
                for (String agentUid1 : unallocatedMemory.getAgentUids()) {
                    for (String agentUid2 : unallocatedMemory.getAgentUids()) {
                        if (agentUid1.equals(agentUid2)) {
                            continue;
                        }
                        
                        memoryToAllocateOnMachine = 
                            calculateMemoryToAllocateOnTwoMachines(
                                    agentUid1,
                                    agentUid2,
                                    memoryShortage);
                        
                        if (memoryToAllocateOnMachine > 0) {
                            
                            allocateCapacityOnMachine(
                                    agentUid1, 
                                    new MemoryCapacityRequirement(memoryToAllocateOnMachine));
                            
                            allocateCapacityOnMachine(
                                    agentUid2, 
                                    new MemoryCapacityRequirement(memoryToAllocateOnMachine));
                            break;
                        }                  
                    }
                    if (memoryToAllocateOnMachine > 0) { 
                        // break from outer for loop
                        break;
                    }
                }
            }
            
            if (memoryToAllocateOnMachine == 0) {
                
                // try allocate memory on a single machine
                for (String agentUid : unallocatedMemory.getAgentUids()) {
                    
                    memoryToAllocateOnMachine = 
                        calculateMemoryToAllocateOnSingleMachine(agentUid, memoryShortage);
                    
                    if (memoryToAllocateOnMachine > 0) {
                            allocateCapacityOnMachine(
                                    agentUid, 
                                    new MemoryCapacityRequirement(memoryToAllocateOnMachine));
                            break;
                    }
                }
            }
            
        } while (memoryToAllocateOnMachine > 0);
    }

    /**
     * @return the memory to allocate on each machine to satisfy the specified memory shortage.
     * 
     */
    private long calculateMemoryToAllocateOnTwoMachines(String agentUid2, String agentUid1, long memoryShortage) {
        
        long memoryToAllocateOnMachine1 = 
            calculateMemoryToAllocateOnMachine(agentUid1, (int) Math.floor(memoryShortage/2.0));
        long memoryToAllocateOnMachine2 = 
            calculateMemoryToAllocateOnMachine(agentUid2, memoryToAllocateOnMachine1);
        long memoryToAllocateOnEachMachine = Math.min(memoryToAllocateOnMachine1,memoryToAllocateOnMachine2);
        
        long allocatedMemoryForPuOnMachine1 = getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agentUid1));
        long allocatedMemoryForPuOnMachine2 = getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agentUid2));
        
        for(; memoryToAllocateOnEachMachine > 0; memoryToAllocateOnEachMachine -= this.containerMemoryCapacityInMB) {
            long maxMemoryPerMachine = getMaxMemoryPerMachine(memoryToAllocateOnEachMachine*2);
            if (memoryToAllocateOnEachMachine + allocatedMemoryForPuOnMachine1 <= maxMemoryPerMachine && 
                memoryToAllocateOnEachMachine + allocatedMemoryForPuOnMachine2 <= maxMemoryPerMachine) {
                break;
            }
        }
        return memoryToAllocateOnEachMachine;
    }

    /**
     * @return the memory to allocate on the specified machine to satisfy the specified memory shortage.
     */
    private long calculateMemoryToAllocateOnSingleMachine(String agentUid, long memoryShortage) {
        
        long memoryToAllocateOnMachine = 
            calculateMemoryToAllocateOnMachine(agentUid, memoryShortage);
        
        long allocatedMemoryForPuOnMachine1 = getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agentUid));
        for(; memoryToAllocateOnMachine > 0; memoryToAllocateOnMachine -= this.containerMemoryCapacityInMB) {
            long maxMemoryPerMachine = getMaxMemoryPerMachine(memoryToAllocateOnMachine);
            if (memoryToAllocateOnMachine + allocatedMemoryForPuOnMachine1 <= maxMemoryPerMachine) { 
                break;
            }
        }
        return memoryToAllocateOnMachine;
    }

    private long calculateMemoryToAllocateOnMachine(String agentUid, long memoryShortage) {
        
        memoryShortage  -= memoryShortage % containerMemoryCapacityInMB;
        final long unallocatedMemoryOnMachine = getMemoryInMB(unallocatedCapacity.getAgentCapacityOrZero(agentUid));
        if (unallocatedMemoryOnMachine % containerMemoryCapacityInMB > 0) {
            throw new IllegalStateException("unallocated memory " + containerMemoryCapacityInMB + "MB must be in multiples of " + containerMemoryCapacityInMB);
        }
        
        long memoryToAllocateOnMachine = Math.min(memoryShortage,unallocatedMemoryOnMachine);
        return memoryToAllocateOnMachine;
    }

    private void allocateNewCapacityForPu(CapacityRequirements goalCapacity, boolean startExtraContainersToSatisfyNonMemoryShortage) {
        
        if (getMemoryInMB(goalCapacity) % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("goalCapacity memory (" + getMemoryInMB(goalCapacity) + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
        }
        
        // for high availability purposes a failure of one machine should not take down more than half of the cluster (assuming minMachines=2)
        
        for (String agentUid : unallocatedCapacity.getAgentUids()) {
            
            CapacityRequirements allocateOnMachine = new CapacityRequirements();
            for (CapacityRequirement goalCapacityRequirement : goalCapacity.getRequirements()) {
                
                if (goalCapacityRequirement instanceof MemoryCapacityRequirement) {
                    //ignore
                    continue;
                }
                
                long unallocatedMemoryOnMachine = getMemoryInMB(unallocatedCapacity.getAgentCapacityOrZero(agentUid));
                long allocatedMemoryForPuOnMachine = 
                    getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agentUid).add(allocateOnMachine));
                    
                
                CapacityRequirement capacityShortage = 
                    goalCapacityRequirement.subtract(
                            allocatedCapacityForPu.getTotalAllocatedCapacity().getRequirement(goalCapacityRequirement.getType()));
            
                // calculate capacity to allocate on machine based on constraints (available free capacity)
                CapacityRequirement unallocatedCapacityOnMachine = unallocatedCapacity.getAgentCapacity(agentUid).getRequirement(goalCapacityRequirement.getType());
                CapacityRequirement capacityToAllocateOnMachine = capacityShortage.min(unallocatedCapacityOnMachine);
                
                // check if trying to allocate non-memory capacity, and the machine has no containers on it (no memory allocated)
                if ( !capacityToAllocateOnMachine.equalsZero()) {
                     
                    if (//need to allocate a new container on this machine
                        allocatedMemoryForPuOnMachine == 0 && 
                            
                        //enough free memory to allocate container
                        unallocatedMemoryOnMachine >= containerMemoryCapacityInMB && 
                            
                         //can start a container on a new machine
                         startExtraContainersToSatisfyNonMemoryShortage &&
                            
                         //pu not stretched out to max number of containers
                         allocatedCapacityForPu.getAgentUids().size() < maxMemoryCapacityInMB/containerMemoryCapacityInMB) {
                        
                        // allocate memory (start a new container for the non-memory allocation)
                        allocatedMemoryForPuOnMachine = containerMemoryCapacityInMB;
                        allocateOnMachine = allocateOnMachine.add(new MemoryCapacityRequirement(containerMemoryCapacityInMB));
                    }
                    
                    if (allocatedMemoryForPuOnMachine > 0) {
                        allocateOnMachine = allocateOnMachine.add(capacityToAllocateOnMachine);
                    }
                }
            }
            allocateCapacityOnMachine(agentUid, allocateOnMachine);
        }
    }

    private long getMaxMemoryPerMachine(long additionalMemoryToAllocate) {
        long totalMemory = getMemoryInMB(allocatedCapacityForPu)+additionalMemoryToAllocate;
        return Math.max(containerMemoryCapacityInMB, (long) Math.ceil(totalMemory/(1.0*minimumNumberOfMachines)));
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
    void rebalanceExistingContainers() {
        
        rebalanceMemoryCapacity();
        rebalanceNonMemoryCapacity();
    }

    /**
     * Balance memory between allocated machines.
     * Do not allocate memory on a new machine!
     */
    private void rebalanceMemoryCapacity() {
        
        long allocatedMemory = getMemoryInMB(allocatedCapacityForPu);
        
        int numberOfContainers = (int) (allocatedMemory / containerMemoryCapacityInMB);
        int numberOfMachines = allocatedCapacityForPu.getAgentUids().size();
        
        int minNumberOfContainersPerMachine = (int) Math.floor(1.0*numberOfContainers/numberOfMachines);
        int maxNumberOfContainersPerMachine = (int) Math.ceil(1.0*numberOfContainers/numberOfMachines);
        
        final long minMemoryPerMachine = minNumberOfContainersPerMachine * containerMemoryCapacityInMB;
        final long maxMemoryPerMachine = maxNumberOfContainersPerMachine * containerMemoryCapacityInMB;
        
        List<String> sortedAgentUids = new ArrayList<String>(allocatedCapacityForPu.getAgentUids());
        Collections.sort(sortedAgentUids,new Comparator<String>() {

            public int compare(String agent1, String agent2) {
                
                long weight1 = 
                    calcMemoryExcessAboveAverage(maxMemoryPerMachine, agent1) - 
                    calcMemoryShortageBelowAverage(minMemoryPerMachine, agent1);
                
                long weight2 = 
                    calcMemoryExcessAboveAverage(maxMemoryPerMachine, agent2) - 
                    calcMemoryShortageBelowAverage(minMemoryPerMachine, agent2);
                
                return (int) Math.signum(weight1 - weight2);
            }
        });
     
        int targetIndex = 0;
        int sourceIndex = sortedAgentUids.size()-1;
        while (targetIndex < sourceIndex) {
            
            String sourceAgentUid = sortedAgentUids.get(sourceIndex);
            String targetAgentUid = sortedAgentUids.get(targetIndex);
            
            long sourceMemoryExcess = calcMemoryExcessAboveAverage(maxMemoryPerMachine, sourceAgentUid);
            long targetMemoryShortage = calcMemoryShortageBelowAverage(minMemoryPerMachine, targetAgentUid);
            
            long memoryToRelocate = 0;
            if (sourceMemoryExcess == 0 && targetMemoryShortage == 0) {
                sourceIndex--;
                targetIndex++;
            }
            else if (sourceMemoryExcess > 0 &&
                sourceMemoryExcess > targetMemoryShortage) {
                
                long freeMemoryOnTarget = 
                    calcMemoryShortageBelowAverage(maxMemoryPerMachine, targetAgentUid);

                if (freeMemoryOnTarget <= sourceMemoryExcess) {
                    memoryToRelocate = freeMemoryOnTarget;
                    // target is maximum, move on
                    targetIndex++;
                }
                else {
                    memoryToRelocate = sourceMemoryExcess;
                    sourceIndex--;
                }
            }
            else if (targetMemoryShortage > 0){
               long movableMemoryOnSource = 
                    calcMemoryExcessAboveAverage(minMemoryPerMachine, sourceAgentUid);
               
               if (movableMemoryOnSource <= targetMemoryShortage) {
                   memoryToRelocate = movableMemoryOnSource;
                   // source is minimum, move on
                   sourceIndex--;
               }
               else {
                   memoryToRelocate = targetMemoryShortage;
                   targetIndex++;
               }
            }
            
            memoryToRelocate -= memoryToRelocate % containerMemoryCapacityInMB;
            if (memoryToRelocate > 0) {
                deallocateCapacityOnMachine(sourceAgentUid, new MemoryCapacityRequirement(memoryToRelocate));
                allocateCapacityOnMachine(targetAgentUid, new MemoryCapacityRequirement(memoryToRelocate));
            }
            
        }
    }

    private long calcMemoryExcessAboveAverage(long average, String agent) {
        
        if (average % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("average does not divide by " + containerMemoryCapacityInMB);
        }
        
        long allocated = getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agent));
        
        return Math.max(
                allocated - average , 
                0);

    }

    private long calcMemoryShortageBelowAverage(final long average, String agent) {
        
        if (average % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("average does not divide by " + containerMemoryCapacityInMB);
        }
        
        long shortage = Math.max( 
                average - getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agent)), 
                0);
        
        long unallocated = 
            roundMemoryToContainerMemory(getMemoryInMB(unallocatedCapacity.getAgentCapacityOrZero(agent)));
                
        return Math.min(
                 shortage,
                 unallocated);
    }
    
    /**
     * Rebalance capacity without moving containers.
     */
    private void rebalanceNonMemoryCapacity() {
        
        int numberOfContainers = (int) (getMemoryInMB(allocatedCapacityForPu) / containerMemoryCapacityInMB);
        CapacityRequirements totalAllocatedCapacity = allocatedCapacityForPu.getTotalAllocatedCapacity();
        for (CapacityRequirement capacityRequirement : totalAllocatedCapacity.getRequirements()) {
            if (capacityRequirement instanceof MemoryCapacityRequirement) {
                // balancing of containers is done separately
                continue;
            }
            CapacityRequirement goalCapacityPerContainer = capacityRequirement.divide(numberOfContainers);
            if (!goalCapacityPerContainer.equalsZero()) {
                for (String sourceAgentUid : allocatedCapacityForPu.getAgentUids()) {
                    relocateNonMemoryCapacityFromSourceMachine(goalCapacityPerContainer, sourceAgentUid);
                }
            }
        }
    }
    
    /**
     * Get the most hard working container
     */
    private CapacityRequirements getMaximumNonMemoryCapacityPerContainer() {
        
        CapacityRequirements maxNonMemoryCapacityPerContainer = new CapacityRequirements();
        for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
            maxNonMemoryCapacityPerContainer =maxNonMemoryCapacityPerContainer.max(getAverageNonMemoryCapacityPerContainer(agentUid,0));
        }
        
        return maxNonMemoryCapacityPerContainer;
    }

    private CapacityRequirements getAverageNonMemoryCapacityPerContainer(String agentUid, int numberOfContainerToRemove) {
        return 
            allocatedCapacityForPu.getAgentCapacity(agentUid)
            .set(new MemoryCapacityRequirement())
            .divide(getNumberOfContainers(agentUid) - numberOfContainerToRemove);
    }

    private int getNumberOfContainers(String agentUid) {
        final long allocatedMemory = getMemoryInMB(allocatedCapacityForPu.getAgentCapacity(agentUid));
        final int numberOfContainers = (int) (allocatedMemory / containerMemoryCapacityInMB);
        return numberOfContainers;
    }

    private CapacityRequirements getAverageNonMemoryCapacityPerContainer() {
        return allocatedCapacityForPu.getTotalAllocatedCapacity()
                .set(new MemoryCapacityRequirement())
                .divide(getNumberOfContainers());
    }

    private int getNumberOfContainers() {
        final long allocatedMemory = getMemoryInMB(allocatedCapacityForPu);
        final int numberOfContainers = (int) (allocatedMemory / containerMemoryCapacityInMB);
        return numberOfContainers;
    }
    
    private void relocateNonMemoryCapacityFromSourceMachine(CapacityRequirement goalCapacityPerContainer, String sourceAgentUid) {
        
        if (goalCapacityPerContainer instanceof MemoryCapacityRequirement) {
            throw new IllegalStateException("This method cannot move containers");
        }
        
        for (String targetAgentUid : unallocatedCapacity.getAgentUids()) {
        
            CapacityRequirementType<? extends CapacityRequirement> goalType = goalCapacityPerContainer.getType();
            CapacityRequirement capacityOnSourceMachine = allocatedCapacityForPu.getAgentCapacity(sourceAgentUid).getRequirement(goalType);
            int numberOfContainersOnSourceMachine = (int) 
                (getMemoryInMB(allocatedCapacityForPu.getAgentCapacity(sourceAgentUid)) / containerMemoryCapacityInMB);
            if (numberOfContainersOnSourceMachine == 0) {
                // no containers on this machine to relocate cpu to
                continue;
            }
            
            CapacityRequirement goalCapacityOnSourceMachine = goalCapacityPerContainer.multiply(numberOfContainersOnSourceMachine);
            CapacityRequirement capacityToRelocateFromSource = capacityOnSourceMachine.subtractOrZero(goalCapacityOnSourceMachine);
            
            if (capacityToRelocateFromSource.equalsZero()) {
                // source already below capacity goal
                break;
            }
            
            if (sourceAgentUid.equals(targetAgentUid)) {
                // cannot relocate to itself
                continue;
            }
            
            CapacityRequirements capacityOnTargetMachine = allocatedCapacityForPu.getAgentCapacityOrZero(targetAgentUid);
            long memoryOnTargetMachine = getMemoryInMB(capacityOnTargetMachine);
            int numberOfContainersOnTargetMachine = (int) (memoryOnTargetMachine / containerMemoryCapacityInMB);
            CapacityRequirement goalCapacityOnTargetMachine = goalCapacityPerContainer.multiply(numberOfContainersOnTargetMachine);
            CapacityRequirement capacityRequiredOnTarget = goalCapacityOnTargetMachine.subtractOrZero(capacityOnTargetMachine.getRequirement(goalType));
            CapacityRequirement unallocatedCapacityOnTarget = unallocatedCapacity.getAgentCapacity(targetAgentUid).getRequirement(goalType);
            CapacityRequirement capacityToRelocateToTarget = 
                capacityToRelocateFromSource 
                .min(capacityRequiredOnTarget)
                .min(unallocatedCapacityOnTarget);
            
            if (capacityToRelocateToTarget.equalsZero()) {
                // the target machine has the maximum number of cpu cores
                // or not enough unallocated cpu cores
                // do not relocate cpu cores from source to target
                continue;
            }
            
            deallocateCapacityOnMachine(sourceAgentUid, capacityToRelocateToTarget);
            allocateCapacityOnMachine(targetAgentUid, capacityToRelocateToTarget);
            
        }
    }

    private CapacityRequirements getGoalCapacityScaleOut(CapacityRequirements capacityToAllocate) {
        
        // Cannot allocate more than unallocated capacity.
        capacityToAllocate = capacityToAllocate.min(unallocatedCapacity.getTotalAllocatedCapacity());
        CapacityRequirements goalCapacity = allocatedCapacityForPu.getTotalAllocatedCapacity().add(capacityToAllocate);
        goalCapacity = roundCeilMemoryToContainerMemory(goalCapacity);
        goalCapacity.max(new MemoryCapacityRequirement(maxMemoryCapacityInMB));
        return goalCapacity;
    }
    
    private CapacityRequirements getGoalCapacityScaleIn(CapacityRequirements capacityToDeallocate) {
        
        CapacityRequirements goalCapacity = allocatedCapacityForPu.getTotalAllocatedCapacity().subtract(capacityToDeallocate);
        goalCapacity = roundCeilMemoryToContainerMemory(goalCapacity);

        long minimumMemoryInMB = minimumNumberOfMachines*containerMemoryCapacityInMB;
        if (getMemoryInMB(goalCapacity) < minimumMemoryInMB) {
           throw new IllegalArgumentException("cannot deallocate " + getMemoryInMB(capacityToDeallocate) + "MB, since only allocated " + getMemoryInMB(allocatedCapacityForPu) + " and minimum memory is "+minimumMemoryInMB);
        }
        
        return goalCapacity;
    }

    private long roundMemoryToContainerMemory(long memoryInMB) {
        final long partialContainerMemory = memoryInMB % containerMemoryCapacityInMB;
        if (partialContainerMemory > 0) {
            memoryInMB = containerMemoryCapacityInMB - partialContainerMemory; 
        }
        return memoryInMB;
    }
    
    private CapacityRequirements roundCeilMemoryToContainerMemory(CapacityRequirements capacity) {
        final long partialContainerMemory = getMemoryInMB(capacity) % containerMemoryCapacityInMB;
        if (partialContainerMemory > 0) {
            capacity = capacity.add(new MemoryCapacityRequirement(containerMemoryCapacityInMB - partialContainerMemory)); 
        }
        return capacity;
    }
    
    private CapacityRequirements roundFloorMemoryToContainerMemory(CapacityRequirements capacity) {
        final long partialContainerMemory = getMemoryInMB(capacity) % containerMemoryCapacityInMB;
        if (partialContainerMemory > 0) {
            capacity = capacity.subtract(new MemoryCapacityRequirement(partialContainerMemory)); 
        }
        return capacity;
    }
    
    private void allocateCapacityOnMachine(String agentUid, CapacityRequirement capacityToAllocateOnAgent) {
        allocateCapacityOnMachine(agentUid, new CapacityRequirements(capacityToAllocateOnAgent));
    }
    
    private void allocateCapacityOnMachine(String agentUid, CapacityRequirements capacityToAllocateOnAgent) {
        
        if (capacityToAllocateOnAgent.equalsZero()) {
            // nothing to do
            return;
        }
        
        if (getMemoryInMB(capacityToAllocateOnAgent) % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("allocatedCapacityOnSourceMachine memory (" + getMemoryInMB(capacityToAllocateOnAgent) + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
        }
        
        // un-deallocate capacity first before allocating
        CapacityRequirements deallocated = capacityToAllocateOnAgent.min(deallocatedCapacityResult.getAgentCapacityOrZero(agentUid));
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
    
    private void deallocateCapacityOnMachine(String agentUid, CapacityRequirement capacityToDeallocateOnAgent) {
        deallocateCapacityOnMachine(agentUid, new CapacityRequirements(capacityToDeallocateOnAgent));
    }
    
    private void deallocateCapacityOnMachine(String agentUid, CapacityRequirements capacityToDeallocateOnAgent) {
        
        if (getMemoryInMB(capacityToDeallocateOnAgent) % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("allocatedCapacityOnSourceMachine memory (" + getMemoryInMB(capacityToDeallocateOnAgent) + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
        }
        
        // un-allocate capacity first before deallocating
        CapacityRequirements allocated = capacityToDeallocateOnAgent.min(allocatedCapacityResult.getAgentCapacityOrZero(agentUid));
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
    
    public void solveNumberOfMachines(int numberOfMachines) {
        boolean success = false;
        try {
            debugTrace = "BinPackingSolver: number of machines " + numberOfMachines;
            validateInput();
            
            unallocatedCapacity = roundFloorMemoryToContainerMemory(unallocatedCapacity);
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
                CapacityRequirements capacityToAllocateOnAgent = new CapacityRequirements(new MemoryCapacityRequirement(containerMemoryCapacityInMB));
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
            
            debugTrace += 
                " allocatedCapacityResult=" + this.getAllocatedCapacityResult().toDetailedString() + 
                " deallocatedCapacityResult="+this.getDeallocatedCapacityResult().toDetailedString();
            
            success = true;
        }
        finally {
            if (logger.isDebugEnabled()) {
                logger.debug(debugTrace);
            }
            else if (!success && logger.isInfoEnabled()) {
                logger.info(debugTrace);
            }
        }
    }

    private void removeContainerFromMachineWithMoreThanOneContainerAndMaxUnallocatedMemory() {
        long maxUnallocatedMemoryInMB = Long.MAX_VALUE;
        String agentUidToRemoveContainer = null;
        
        for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
            long unallocated = getMemoryInMB(unallocatedCapacity.getAgentCapacityOrZero(agentUid));
            long allocatedByPu = getMemoryInMB(allocatedCapacityForPu.getAgentCapacity(agentUid));
            if (allocatedByPu >= containerMemoryCapacityInMB*2 && maxUnallocatedMemoryInMB < unallocated) {
                maxUnallocatedMemoryInMB = unallocated;
                agentUidToRemoveContainer = agentUid;
            }
        }
        
        if (agentUidToRemoveContainer != null) {
            CapacityRequirements capacityToRemove = new CapacityRequirements(new MemoryCapacityRequirement(containerMemoryCapacityInMB));
            if (!allocatedCapacityResult.getAgentCapacity(agentUidToRemoveContainer).equalsZero()) {
                throw new IllegalStateException("Impossible to allocate and deallocate from the same agent " + agentUidToRemoveContainer);
            }
            deallocatedCapacityResult = deallocatedCapacityResult.add(agentUidToRemoveContainer, capacityToRemove);
            allocatedCapacityForPu = allocatedCapacityForPu.subtract(agentUidToRemoveContainer, capacityToRemove);
            //leave unallocated capacity for this agent deleted so it wont be allocated
        }
        
    }

    private boolean isNewContainerWillBreachMaximumMemory() {
        return getMemoryInMB(allocatedCapacityForPu) + containerMemoryCapacityInMB > maxMemoryCapacityInMB;
    }

    private void validateInput() {
        if (allocatedCapacityForPu == null) {
            throw new IllegalArgumentException("allocatedCapacityForPu");
        }
        
        if (unallocatedCapacity == null) {
            throw new IllegalArgumentException("unallocatedCapacity");
        }

        debugTrace += 
            " containerMemoryCapacityInMB=" + containerMemoryCapacityInMB + 
            " maxMemoryCapacityInMB=" + maxMemoryCapacityInMB + 
            " unallocatedCapacity=" + unallocatedCapacity.toDetailedString() +
            " allocatedCapacityForPu=" + allocatedCapacityForPu.toDetailedString() + 
            " minimumNumberOfMachines=" + + minimumNumberOfMachines;
        
        if (containerMemoryCapacityInMB == 0) {
            throw new IllegalArgumentException("containerMemoryCapacityInMB");
        }
        
        if (maxMemoryCapacityInMB == 0) {
            throw new IllegalArgumentException("maxMemoryCapacityInMB");
        }
        
        if (maxMemoryCapacityInMB % containerMemoryCapacityInMB != 0) {
            throw new IllegalArgumentException("max memory capacity must divide by " + containerMemoryCapacityInMB);
        }
        
        if (getMemoryInMB(allocatedCapacityForPu) > maxMemoryCapacityInMB) {
            throw new IllegalArgumentException("total PU allocated capacity exceeds the specified max number of containers");
        }
        
       for (String agentUid : allocatedCapacityForPu.getAgentUids()) {
           CapacityRequirements agentCapacity = allocatedCapacityForPu.getAgentCapacity(agentUid);
           if (getMemoryInMB(agentCapacity) % containerMemoryCapacityInMB != 0) {
               throw new IllegalArgumentException("agentCapacity memory (" + getMemoryInMB(agentCapacity) + "MB) must divide by containerMemoryCapacityInMB (" + containerMemoryCapacityInMB + "MB)");
           }
       }
        
        if (minimumNumberOfMachines < 0) {
            throw new IllegalArgumentException("minimumNumberOfMachines");
        }
    }
    
    private String findFreeAgentUid(CapacityRequirements capacityToAllocateOnAgent) {
        
        logger.debug("Looking for an agent to allocate " + capacityToAllocateOnAgent);
        
        long minUnallocatedMemoryInMB = Long.MAX_VALUE;
        String chosenAgentUid = null;
        
        for (String agentUid : unallocatedCapacity.getAgentUids()) {
            
            if (getMemoryInMB(capacityToAllocateOnAgent) == 0 &&
                (!allocatedCapacityForPu.getAgentUids().contains(agentUid) ||
                 getMemoryInMB(allocatedCapacityForPu.getAgentCapacityOrZero(agentUid)) == 0)) {
                    logger.debug("Cannot allocate on agent " + agentUid + " without memory, since it does not already have any memory allocated");
                    continue;
            }
            
            CapacityRequirements unallocatedCapacityOnAgent = unallocatedCapacity.getAgentCapacity(agentUid);
            
            if (!unallocatedCapacityOnAgent.greaterOrEquals(capacityToAllocateOnAgent)) {
                logger.debug("Cannot allocate on agent " + agentUid + " since it does not have enough unallocated capacity");
                continue;
            }
            
            // The best fit agent, has the least unallocated memory. 
            // This is to maximize the size of continuous unallocated memory blocks in other agents (bin-packing Tetris heuristics)
            if (minUnallocatedMemoryInMB > getMemoryInMB(unallocatedCapacityOnAgent)) {
                
                chosenAgentUid = agentUid;
                minUnallocatedMemoryInMB = getMemoryInMB(unallocatedCapacityOnAgent);
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

    public ClusterCapacityRequirements getAllocatedCapacityResult() {
        return allocatedCapacityResult;
    }
    
    public ClusterCapacityRequirements getDeallocatedCapacityResult() {
        return deallocatedCapacityResult;
    }

    public ClusterCapacityRequirements getAllocatedCapacityForPu() {
        return allocatedCapacityForPu;
    }
    
    public ClusterCapacityRequirements getUnallocatedCapacity() {
        return unallocatedCapacity;
    }

    /**
     * cleans AllocatedResult and DeallocatedResult
     * Used for unit testing
     */
    public void reset() {
        debugTrace = "";
        this.deallocatedCapacityResult = new ClusterCapacityRequirements();
        this.allocatedCapacityResult = new ClusterCapacityRequirements();
    }
    
    private long getMemoryInMB(ClusterCapacityRequirements clusterCapacity) {
        return getMemoryInMB(clusterCapacity.getTotalAllocatedCapacity());
    }
    
    private long getMemoryInMB(CapacityRequirements capacity) {
        return capacity.getRequirement(new MemoryCapacityRequirement().getType()).getMemoryInMB();
    }
    
}
