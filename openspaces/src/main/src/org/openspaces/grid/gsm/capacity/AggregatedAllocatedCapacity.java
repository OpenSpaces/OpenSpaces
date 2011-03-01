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

    @Override
    public boolean equals(Object other) {
        return 
            other instanceof AggregatedAllocatedCapacity &&
            ((AggregatedAllocatedCapacity)other).capacityPerAgent.equals(capacityPerAgent);
    }

    public Collection<String> getAgentUids() {
        return capacityPerAgent.keySet();
    }
    
    @Override
    public String toString() {
        return capacityPerAgent.size() + " machines with total capacity of " + getTotalAllocatedCapacity();
    }
    
    public String toDetailedString() {
        StringBuilder builder = new StringBuilder();
        builder.append("totalNumberOfMachines:" + capacityPerAgent.size() + " , totalCapacity:" + getTotalAllocatedCapacity()+", details:{");
        for (String agentUid : capacityPerAgent.keySet()) {
            builder.append(agentUid + ":" + capacityPerAgent.get(agentUid)+" , ");
        }
        builder.append("}");
        return builder.toString();
    }
    
    public AggregatedAllocatedCapacity add(AggregatedAllocatedCapacity other) {
        if (other.equalsZero()) {
            return this;
        }
        
        AggregatedAllocatedCapacity sum = new AggregatedAllocatedCapacity();
        sum.addAllInternal(this);
        sum.addAllInternal(other);
        return sum;
    }

    public AggregatedAllocatedCapacity subtract(
            AggregatedAllocatedCapacity other) {

        AggregatedAllocatedCapacity diff = new AggregatedAllocatedCapacity();
        diff.addAllInternal(this);
        diff.subtractAllInternal(other);
        return diff;
    }
    
    public AggregatedAllocatedCapacity add(
            String agentUid, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity sum = new AggregatedAllocatedCapacity();
        sum.addAllInternal(this);
        sum.addInternal(agentUid,capacity);
        return sum;
        
    }
    
    public AggregatedAllocatedCapacity subtract(
            String agentUid, 
            AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity remaining = new AggregatedAllocatedCapacity();
        remaining.addAllInternal(this);
        remaining.subtractInternal(agentUid,capacity);
        return remaining;
    }



    public AggregatedAllocatedCapacity subtractAgent(
            String agentUid) {
        return subtract(agentUid, this.getAgentCapacity(agentUid));
    }
    
    public AggregatedAllocatedCapacity subtractOrZero(
           String agentUid, AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity remaining = new AggregatedAllocatedCapacity();
        remaining.addAllInternal(this);
        remaining.subtractOrZeroInternal(agentUid,capacity);
        return remaining;
    }


    public AllocatedCapacity getAgentCapacity(String agentUid) {
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException(agentUid);
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
    
    private void addAllInternal(AggregatedAllocatedCapacity aggregatedCapacity) {
        for (String agentUid : aggregatedCapacity.capacityPerAgent.keySet()) {
            AllocatedCapacity capacity = aggregatedCapacity.capacityPerAgent.get(agentUid);
            addInternal(agentUid,capacity);
        }
    }
    
    private void subtractAllInternal(AggregatedAllocatedCapacity aggregatedCapacity) {
        for (String agentUid : aggregatedCapacity.capacityPerAgent.keySet()) {
            AllocatedCapacity capacity = aggregatedCapacity.capacityPerAgent.get(agentUid);
            subtractInternal(agentUid,capacity);
        }
    }
    
    private void addInternal(String agentUid, AllocatedCapacity capacityToAdd) {
        
        validateAllocation(capacityToAdd);
        AllocatedCapacity sumCapacity = capacityToAdd;
        if (capacityPerAgent.containsKey(agentUid)) {
            
            sumCapacity = sumCapacity.add(capacityPerAgent.get(agentUid));
        }
        
        capacityPerAgent.put(agentUid,sumCapacity);
        totalCapacity = totalCapacity.add(capacityToAdd);
    }

  
    private void subtractInternal(String agentUid, AllocatedCapacity capacity) {
        
        validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("Agent UID " + agentUid + " no found");
        }
        
        AllocatedCapacity newAllocation = 
            capacityPerAgent.get(agentUid).subtract(capacity);
        
        updateAgentCapacity(agentUid, newAllocation);
        
        totalCapacity = totalCapacity.subtract(capacity);
    }
    

    private void subtractOrZeroInternal(String agentUid, AllocatedCapacity capacity) {
   validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("Agent UID " + agentUid + " no found");
        }
        
        AllocatedCapacity newAllocation = 
            capacityPerAgent.get(agentUid).subtractOrZero(capacity);
        
        updateAgentCapacity(agentUid, newAllocation);
        totalCapacity = totalCapacity.subtract(capacity);
        
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
