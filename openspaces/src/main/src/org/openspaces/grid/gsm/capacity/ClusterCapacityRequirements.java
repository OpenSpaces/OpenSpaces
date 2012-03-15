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
package org.openspaces.grid.gsm.capacity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClusterCapacityRequirements {

    // allocated capacity per grid service agent (UUID)
    private final Map<String,CapacityRequirements> capacityPerAgent;
    private CapacityRequirements totalCapacity;
    
    public ClusterCapacityRequirements() {
        // use consistent ordering of machines so unit tests and bugs will have consistent iterator behavior.
        this.capacityPerAgent = new TreeMap<String, CapacityRequirements>();
        totalCapacity = new CapacityRequirements();
    }
        
    public CapacityRequirements getTotalAllocatedCapacity() {
        return totalCapacity;
    }
    
    public boolean equalsZero() {
        return capacityPerAgent.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return 
            other instanceof ClusterCapacityRequirements &&
            ((ClusterCapacityRequirements)other).capacityPerAgent.equals(capacityPerAgent);
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
        List<String> keySet = new ArrayList<String>(capacityPerAgent.keySet());
        Collections.sort(keySet);
        for (String agentUid : keySet) {
            builder.append(agentUid + ":" + capacityPerAgent.get(agentUid)+" , ");
        }
        builder.append("}");
        return builder.toString();
    }
    
    public ClusterCapacityRequirements add(ClusterCapacityRequirements other) {
        if (other.equalsZero()) {
            return this;
        }
        
        ClusterCapacityRequirements sum = new ClusterCapacityRequirements();
        sum.addAllInternal(this);
        sum.addAllInternal(other);
        return sum;
    }

    public ClusterCapacityRequirements subtract(
            ClusterCapacityRequirements other) {

        ClusterCapacityRequirements diff = new ClusterCapacityRequirements();
        diff.addAllInternal(this);
        diff.subtractAllInternal(other);
        return diff;
    }
    
    public ClusterCapacityRequirements set(String agentUid, CapacityRequirements capacity) {
        ClusterCapacityRequirements sum = new ClusterCapacityRequirements();
        sum.addAllInternal(this);
        sum.setInternal(agentUid,capacity);
        return sum;
    }
    
    public ClusterCapacityRequirements add(
            String agentUid, 
            CapacityRequirements capacity) {
        
        ClusterCapacityRequirements sum = new ClusterCapacityRequirements();
        sum.addAllInternal(this);
        sum.addInternal(agentUid,capacity);
        return sum;
        
    }
    
    public ClusterCapacityRequirements subtract(
            String agentUid, 
            CapacityRequirements capacity) {
        
        ClusterCapacityRequirements remaining = new ClusterCapacityRequirements();
        remaining.addAllInternal(this);
        remaining.subtractInternal(agentUid,capacity);
        return remaining;
    }



    public ClusterCapacityRequirements subtractAgent(
            String agentUid) {
        return subtract(agentUid, this.getAgentCapacity(agentUid));
    }
    
    public ClusterCapacityRequirements subtractOrZero(
           String agentUid, CapacityRequirements capacity) {
        
        ClusterCapacityRequirements remaining = new ClusterCapacityRequirements();
        remaining.addAllInternal(this);
        remaining.subtractOrZeroInternal(agentUid,capacity);
        return remaining;
    }


    public CapacityRequirements getAgentCapacity(String agentUid) {
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException(agentUid);
        }
        return this.capacityPerAgent.get(agentUid);
    }
    
    public CapacityRequirements getAgentCapacityOrZero(String agentUid) {
        
        if (capacityPerAgent.containsKey(agentUid)) {
            return this.capacityPerAgent.get(agentUid);
        }
        else {
            return new CapacityRequirements();
        }
    }
    
    private void addAllInternal(ClusterCapacityRequirements clusterCapacityRequirements) {
        for (String agentUid : clusterCapacityRequirements.capacityPerAgent.keySet()) {
            CapacityRequirements capacity = clusterCapacityRequirements.capacityPerAgent.get(agentUid);
            addInternal(agentUid,capacity);
        }
    }
    
    private void subtractAllInternal(ClusterCapacityRequirements aggregatedCapacity) {
        for (String agentUid : aggregatedCapacity.capacityPerAgent.keySet()) {
            CapacityRequirements capacity = aggregatedCapacity.capacityPerAgent.get(agentUid);
            subtractInternal(agentUid,capacity);
        }
    }
    
    private void setInternal(String agentUid, CapacityRequirements newCapacity) {

        CapacityRequirements oldCapacity = capacityPerAgent.get(agentUid);
        
        if (newCapacity.equalsZero()) {
            capacityPerAgent.remove(agentUid);
        }
        else {
            capacityPerAgent.put(agentUid,newCapacity);
        }
        
        totalCapacity = totalCapacity.subtract(oldCapacity).add(newCapacity);
    
    }

    private void addInternal(String agentUid, CapacityRequirements capacityToAdd) {
        
        validateAllocation(capacityToAdd);
        CapacityRequirements sumCapacity = capacityToAdd;
        if (capacityPerAgent.containsKey(agentUid)) {
            
            sumCapacity = sumCapacity.add(capacityPerAgent.get(agentUid));
        }
        
        capacityPerAgent.put(agentUid,sumCapacity);
        totalCapacity = totalCapacity.add(capacityToAdd);
    }

  
    private void subtractInternal(String agentUid, CapacityRequirements capacity) {
        
        validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("Agent UID " + agentUid + " no found");
        }
        
        CapacityRequirements newAllocation = 
            capacityPerAgent.get(agentUid).subtract(capacity);
        
        updateAgentCapacity(agentUid, newAllocation);
        
        totalCapacity = totalCapacity.subtract(capacity);
    }
    

    private void subtractOrZeroInternal(String agentUid, CapacityRequirements capacity) {
   validateAllocation(capacity);
        
        if (!capacityPerAgent.containsKey(agentUid)) {
            throw new IllegalArgumentException("Agent UID " + agentUid + " no found");
        }
        
        CapacityRequirements newAllocation = 
            capacityPerAgent.get(agentUid).subtractOrZero(capacity);
        
        updateAgentCapacity(agentUid, newAllocation);
        totalCapacity = totalCapacity.subtract(capacity);
        
    }

    private void updateAgentCapacity(String agentUid, CapacityRequirements newAllocation) {
        if (newAllocation.equalsZero()) {
            capacityPerAgent.remove(agentUid);
        }
        else {
            capacityPerAgent.put(agentUid,newAllocation);
        }
    }
    
    private void validateAllocation(CapacityRequirements allocation) {
        if (allocation.equalsZero()) {
            throw new IllegalArgumentException(allocation + " equals zero");
        }
    }
}
