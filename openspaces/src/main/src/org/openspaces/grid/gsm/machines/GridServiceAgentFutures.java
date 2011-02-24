package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;


class GridServiceAgentFutures {
    
    Collection<FutureGridServiceAgent> futureAgents;
    AllocatedCapacity expectedCapacity = new AllocatedCapacity(Fraction.ZERO,0);
    int expectedNumberOfMachines = 0;
    
    GridServiceAgentFutures(FutureGridServiceAgent[] futureAgents, int numberOfMachines) {
        this.expectedNumberOfMachines = numberOfMachines;
        this.futureAgents = new ArrayList<FutureGridServiceAgent>(Arrays.asList(futureAgents));
    }

    GridServiceAgentFutures(FutureGridServiceAgent[] futureAgents, AllocatedCapacity capacity) {
        expectedCapacity = capacity;
        this.futureAgents = new ArrayList<FutureGridServiceAgent>(Arrays.asList(futureAgents));
    }
    
    GridServiceAgentFutures(FutureGridServiceAgent[] futureAgents, CapacityRequirements capacityRequirements) {
        this.futureAgents = new ArrayList<FutureGridServiceAgent>(Arrays.asList(futureAgents));
        expectedNumberOfMachines = convertCapacityRequirementsToNumberOfMachines(capacityRequirements);
        if (expectedNumberOfMachines == 0) {
            expectedCapacity = convertCapacityRequirementsToAllocatedCapacity(capacityRequirements);
        }
    }
    
    AllocatedCapacity getExpectedCapacity() {
        return expectedCapacity;
    }
    
    int getExpectedNumberOfMachines() {
        return expectedNumberOfMachines;
    }
    
    private static AllocatedCapacity convertCapacityRequirementsToAllocatedCapacity(
            CapacityRequirements capacityRequirements) {
        
        Fraction cpuCores = MachinesSlaUtils.convertCpuCoresFromDoubleToFraction(
                capacityRequirements.getRequirement(CpuCapacityRequirement.class).getCpu());
        long memoryInMB = capacityRequirements.getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
        return new AllocatedCapacity(cpuCores,memoryInMB);
    }

    private static int convertCapacityRequirementsToNumberOfMachines(CapacityRequirements capacityRequirements) {
        
        return capacityRequirements.getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
    }

    public Collection<GridServiceAgent> getGridServiceAgents() {

        Collection<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        // add all future grid service agents
        for (FutureGridServiceAgent futureAgent: futureAgents) {
            
            if (futureAgent.isDone() && futureAgent.getException() != null) {
                
                try {
                    agents.add(futureAgent.get());
                } catch (ExecutionException e) { } 
                catch (TimeoutException e) { }
            }
        }
        
        return agents;
    }

    public boolean isDone() {
        for (FutureGridServiceAgent futureAgent : futureAgents) {
            if (!futureAgent.isDone()) {
                return false;
            }
        }
        return true;
    }

    public Collection<FutureGridServiceAgent> getFutureGridServiceAgents() {
        return Collections.unmodifiableCollection(new ArrayList<FutureGridServiceAgent>(futureAgents));
    }
    
    public void removeFutureAgent(FutureGridServiceAgent futureAgent) {
        if (!futureAgents.contains(futureAgent)) {
            throw new IllegalStateException("futureAgent does not exist");
        }
        futureAgents.remove(futureAgent);
    }
   
    

}
