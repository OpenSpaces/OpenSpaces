package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;


class GridServiceAgentFutures {
    
    Collection<FutureGridServiceAgent> futureAgents;
    CapacityRequirements expectedCapacity = new CapacityRequirements();
        
    GridServiceAgentFutures(FutureGridServiceAgent[] futureAgents, CapacityRequirements capacityRequirements) {
        validate(futureAgents);
        if (capacityRequirements.equalsZero()) {
            throw new IllegalArgumentException("capacityRequirements cannot be empty");
        }
        this.futureAgents = new ArrayList<FutureGridServiceAgent>(Arrays.asList(futureAgents));
        expectedCapacity = capacityRequirements;
    }
    
    CapacityRequirements getExpectedCapacity() {
        return expectedCapacity;
    }
    
    public Collection<GridServiceAgent> getGridServiceAgents() {

        Collection<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        // add all future grid service agents
        for (FutureGridServiceAgent futureAgent: futureAgents) {
            
            if (futureAgent.isDone() && futureAgent.getException() == null) {
                
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
   
    
    private static void validate(FutureGridServiceAgent[] futureAgents) {
        
        if (futureAgents == null) {
            throw new IllegalArgumentException("future agents cannot be null");
        }
        
        if (futureAgents.length == 0) {
            throw new IllegalArgumentException("future agents cannot be empty");
        }
        
        NonBlockingElasticMachineProvisioning machineProvisioning = null;
        for (FutureGridServiceAgent futureAgent : futureAgents) {
            if (machineProvisioning == null) {
                machineProvisioning = futureAgent.getMachineProvisioning();
            }
            if (futureAgent.getMachineProvisioning() != machineProvisioning) {
                throw new IllegalArgumentException("All future agents must origin from the same machine provisioning object");
            }
        }
        
    }
    
    public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
        return futureAgents.iterator().next().getMachineProvisioning();
    }
}
