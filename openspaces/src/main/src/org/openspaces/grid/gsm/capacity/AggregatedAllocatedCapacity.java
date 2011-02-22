package org.openspaces.grid.gsm.capacity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openspaces.core.internal.commons.math.fraction.Fraction;

public class AggregatedAllocatedCapacity {

    // allocated capacity per grid service agent (UUID)
    private final Map<String,AllocatedCapacity> capacityPerAgent;
    private AllocatedCapacity totalCapacity;
    
    public AggregatedAllocatedCapacity() {
        this.capacityPerAgent = new ConcurrentHashMap<String, AllocatedCapacity>();
        totalCapacity = new AllocatedCapacity(Fraction.ZERO, 0);
    }
        
    public AllocatedCapacity getTotalAllocatedCapacity() {
        return totalCapacity;
    }
    
    public boolean equalsZero() {
        return capacityPerAgent.isEmpty();
    }

    public boolean equals(Object other) {
        return 
            other instanceof AggregatedAllocatedCapacity &&
            ((AggregatedAllocatedCapacity)other).capacityPerAgent.equals(capacityPerAgent);
    }

    public Collection<String> getAgentUids() {
        return capacityPerAgent.keySet();
    }
    
    public String toString() {
        return capacityPerAgent.size() + " machines with total capacity of " + getTotalAllocatedCapacity();
    }
    
    public static AggregatedAllocatedCapacity add(
            AggregatedAllocatedCapacity aggregatedCapacity1,
            AggregatedAllocatedCapacity aggregatedCapacity2) {

        AggregatedAllocatedCapacity sum = new AggregatedAllocatedCapacity();
        sum.addAll(aggregatedCapacity1);
        sum.addAll(aggregatedCapacity2);
        return sum;
    }

    public static AggregatedAllocatedCapacity subtract(
            AggregatedAllocatedCapacity aggregatedCapacity1,
            AggregatedAllocatedCapacity aggregatedCapacity2) {

        AggregatedAllocatedCapacity diff = new AggregatedAllocatedCapacity();
        diff.addAll(aggregatedCapacity1);
        diff.subtractAll(aggregatedCapacity2);
        return diff;
    }
    
    public static AggregatedAllocatedCapacity add(
            AggregatedAllocatedCapacity aggregatedCapacity, 
            String agentUid, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity sum = new AggregatedAllocatedCapacity();
        sum.addAll(aggregatedCapacity);
        sum.add(agentUid,capacity);
        return sum;
        
    }
    
    public static AggregatedAllocatedCapacity subtract(
            AggregatedAllocatedCapacity aggregatedCapacity, 
            String agentUid, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity remaining = new AggregatedAllocatedCapacity();
        remaining.addAll(aggregatedCapacity);
        remaining.subtract(agentUid,capacity);
        return remaining;
    }



    public static AggregatedAllocatedCapacity subtractAgent(
            AggregatedAllocatedCapacity aggregatedCapacity,
            String agentUid) {
        return subtract(aggregatedCapacity, agentUid, aggregatedCapacity.getAgentCapacity(agentUid));
    }
    
    public static AggregatedAllocatedCapacity subtractOrZero(
            AggregatedAllocatedCapacity aggregatedCapacity, 
            String agentUid, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity remaining = new AggregatedAllocatedCapacity();
        remaining.addAll(aggregatedCapacity);
        remaining.subtractOrZero(agentUid,capacity);
        return remaining;
    }


    public AllocatedCapacity getAgentCapacity(String agentUid) {
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("agent");
        }
        return this.capacityPerAgent.get(agentUid);
    }
    
    public AllocatedCapacity getAgentCapacityOrZero(String agentUid) {
        
        if (capacityPerAgent.containsKey(agentUid)) {
            return this.capacityPerAgent.get(agentUid);
        }
        else {
            return new AllocatedCapacity(Fraction.ZERO, 0);
        }
    }
    
    private void addAll(AggregatedAllocatedCapacity aggregatedCapacity) {
        for (String agentUid : aggregatedCapacity.capacityPerAgent.keySet()) {
            AllocatedCapacity capacity = aggregatedCapacity.capacityPerAgent.get(agentUid);
            add(agentUid,capacity);
        }
    }
    
    private void subtractAll(AggregatedAllocatedCapacity aggregatedCapacity) {
        for (String agentUid : aggregatedCapacity.capacityPerAgent.keySet()) {
            AllocatedCapacity capacity = aggregatedCapacity.capacityPerAgent.get(agentUid);
            subtract(agentUid,capacity);
        }
    }
    
    private void add(String agentUid, AllocatedCapacity capacityToAdd) {
        
        validateAllocation(capacityToAdd);
        AllocatedCapacity sumCapacity = capacityToAdd;
        if (capacityPerAgent.containsKey(agentUid)) {
            
            sumCapacity = AllocatedCapacity.add(
                    capacityPerAgent.get(agentUid),
                    sumCapacity);
        }
        
        capacityPerAgent.put(agentUid,sumCapacity);
        totalCapacity = AllocatedCapacity.add(totalCapacity, capacityToAdd);
    }

  
    private void subtract(String agentUid, AllocatedCapacity capacity) {
        
        validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("Agent UID " + agentUid + " no found");
        }
        
        AllocatedCapacity newAllocation = 
            AllocatedCapacity.subtract(capacityPerAgent.get(agentUid), capacity);
        
        updateAgentCapacity(agentUid, newAllocation);
        
        totalCapacity = AllocatedCapacity.subtract(totalCapacity, capacity);
    }
    

    private void subtractOrZero(String agentUid, AllocatedCapacity capacity) {
   validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("Agent UID " + agentUid + " no found");
        }
        
        AllocatedCapacity newAllocation = 
            AllocatedCapacity.subtractOrZero(capacityPerAgent.get(agentUid), capacity);
        
        updateAgentCapacity(agentUid, newAllocation);
        totalCapacity = AllocatedCapacity.subtract(totalCapacity, capacity);
        
    }

    private void updateAgentCapacity(String agentUid, AllocatedCapacity newAllocation) {
        if (newAllocation.equalsZero()) {
            capacityPerAgent.remove(agentUid);
        }
        else {
            capacityPerAgent.put(agentUid,newAllocation);
        }
    }
    
    private void validateAllocation(AllocatedCapacity allocation) {
        if (allocation.equalsZero()) {
            throw new IllegalArgumentException(allocation + " equals zero");
        }
    }
}
