package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.core.internal.commons.math.fraction.Fraction;

public class AggregatedAllocatedCapacity {

    private final Map<GridServiceAgent,AllocatedCapacity> capacityPerAgent;
    
    
    public AggregatedAllocatedCapacity() {
        this.capacityPerAgent = new HashMap<GridServiceAgent, AllocatedCapacity>();
    }
        
    public AllocatedCapacity getTotalAllocatedCapacity() {
        AllocatedCapacity total = new AllocatedCapacity(Fraction.ZERO, 0);
        for (GridServiceAgent agent : capacityPerAgent.keySet()) {
            AllocatedCapacity capacity = capacityPerAgent.get(agent);
            total = AllocatedCapacity.add(total,capacity);
        }
        return total;
    }
    
    public boolean equalsZero() {
        return capacityPerAgent.isEmpty();
    }


    public Collection<GridServiceAgent> getAgents() {
        return Collections.unmodifiableCollection(
                new ArrayList<GridServiceAgent>(capacityPerAgent.keySet()));
    }
    
    public String toString() {
        return capacityPerAgent.size() + " machines with total capacity of " + getTotalAllocatedCapacity();
    }
    public static AggregatedAllocatedCapacity add(
            AggregatedAllocatedCapacity aggregatedCapacity1,
            AggregatedAllocatedCapacity aggregatedCapacity2) {

        AggregatedAllocatedCapacity sum = new AggregatedAllocatedCapacity();
        sum.addAll(aggregatedCapacity2);
        return sum;
    }
    
    public static AggregatedAllocatedCapacity add(
            AggregatedAllocatedCapacity aggregatedCapacity, 
            GridServiceAgent agent, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity sum = new AggregatedAllocatedCapacity();
        sum.addAll(aggregatedCapacity);
        sum.add(agent,capacity);
        return sum;
        
    }
    
    public static AggregatedAllocatedCapacity subtract(
            AggregatedAllocatedCapacity aggregatedCapacity, 
            GridServiceAgent agent, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity remaining = new AggregatedAllocatedCapacity();
        remaining.addAll(aggregatedCapacity);
        remaining.subtract(agent,capacity);
        return remaining;
    }

    public AllocatedCapacity getAgentCapacity(GridServiceAgent agent) {
        if (!capacityPerAgent.containsKey(agent)) {
            throw new IllegalArgumentException("agent");
        }
        return this.capacityPerAgent.get(agent);
    }
    
    private void addAll(AggregatedAllocatedCapacity aggregatedCapacity) {
        for (GridServiceAgent agent: aggregatedCapacity.capacityPerAgent.keySet()) {
            AllocatedCapacity capacity = aggregatedCapacity.capacityPerAgent.get(agent);
            add(agent,capacity);
        }
    }
    
    private void add(GridServiceAgent agent, AllocatedCapacity capacity) {
        
        validateAllocation(capacity);
        
        if (capacityPerAgent.containsKey(agent)) {
            
            capacity = 
                AllocatedCapacity.add(
                        capacityPerAgent.get(agent),
                        capacity);
        }
        
        capacityPerAgent.put(agent,capacity);
    }

  
    private void subtract(GridServiceAgent agent, AllocatedCapacity capacity) {
        
        validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agent)) {
            throw new IllegalArgumentException(agent.getMachine().getHostAddress() + " no found");
        }
        
        AllocatedCapacity newAllocation = 
            AllocatedCapacity.subtract(capacityPerAgent.get(agent), capacity);
        
        if (newAllocation.equalsZero()) {
            capacityPerAgent.remove(agent);
        }
        else {
            capacityPerAgent.put(agent,newAllocation);
        }        
    }
    
    private void validateAllocation(AllocatedCapacity allocation) {
        if (allocation.equalsZero()) {
            throw new IllegalArgumentException(allocation + " equals zero");
        }
    }
}
