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
 * WITHOUAbstractCapacityRequirementsPerKey WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.capacity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractCapacityRequirementsPerKey {

    // allocated capacity per key
    private final Map<String,CapacityRequirements> capacityPerKey;
    private CapacityRequirements totalCapacity;
    
    protected AbstractCapacityRequirementsPerKey() {
        // use consistent ordering of machines so unit tests and bugs will have consistent iterator behavior.
        this.capacityPerKey = new HashMap<String, CapacityRequirements>();
        totalCapacity = new CapacityRequirements();
    }
        
    public CapacityRequirements getTotalAllocatedCapacity() {
        return totalCapacity;
    }
    
    public boolean equalsZero() {
        return capacityPerKey.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capacityPerKey == null) ? 0 : capacityPerKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractCapacityRequirementsPerKey other = (AbstractCapacityRequirementsPerKey) obj;
        if (capacityPerKey == null) {
            if (other.capacityPerKey != null)
                return false;
        } else if (!capacityPerKey.equals(other.capacityPerKey))
            return false;
        return true;
    }

    protected Collection<String> getKeys() {
        return capacityPerKey.keySet();
    }
    
    @Override
    public abstract String toString();
    
    public abstract String toDetailedString();
     
    
    protected AbstractCapacityRequirementsPerKey add(AbstractCapacityRequirementsPerKey other) {
        if (other.equalsZero()) {
            return this;
        }
        
        AbstractCapacityRequirementsPerKey sum = newZeroInstance();
        sum.addAllInternal(this);
        sum.addAllInternal(other);
        return sum;
    }

    protected AbstractCapacityRequirementsPerKey subtract(
            AbstractCapacityRequirementsPerKey other) {

        AbstractCapacityRequirementsPerKey diff = newZeroInstance();
        diff.addAllInternal(this);
        diff.subtractAllInternal(other);
        return diff;
    }

    protected abstract AbstractCapacityRequirementsPerKey newZeroInstance();
    
    protected AbstractCapacityRequirementsPerKey set(String key, CapacityRequirements capacity) {
        AbstractCapacityRequirementsPerKey sum = newZeroInstance();
        sum.addAllInternal(this);
        sum.setInternal(key,capacity);
        return sum;
    }
    
    protected AbstractCapacityRequirementsPerKey add(
            String key, 
            CapacityRequirements capacity) {
        
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        
        if (key.length() == 0) {
            throw new IllegalArgumentException("key cannot be empty");
        }
        
        AbstractCapacityRequirementsPerKey sum = newZeroInstance();
        sum.addAllInternal(this);
        sum.addInternal(key,capacity);
        return sum;
        
    }
    
    protected AbstractCapacityRequirementsPerKey subtract(
            String key, 
            CapacityRequirements capacity) {
        
        AbstractCapacityRequirementsPerKey remaining = newZeroInstance();
        remaining.addAllInternal(this);
        remaining.subtractInternal(key,capacity);
        return remaining;
    }



    protected AbstractCapacityRequirementsPerKey subtractKey(
            String key) {
        return subtract(key, this.getKeyCapacity(key));
    }
    
    protected AbstractCapacityRequirementsPerKey subtractOrZero(
           String key, CapacityRequirements capacity) {
        
        AbstractCapacityRequirementsPerKey remaining = newZeroInstance();
        remaining.addAllInternal(this);
        remaining.subtractOrZeroInternal(key,capacity);
        return remaining;
    }


    protected CapacityRequirements getKeyCapacity(String key) {
        if (!capacityPerKey.containsKey(key)) {
            throw new IllegalArgumentException(key);
        }
        return this.capacityPerKey.get(key);
    }
    
    protected CapacityRequirements getKeyCapacityOrZero(String key) {
        
        if (capacityPerKey.containsKey(key)) {
            return this.capacityPerKey.get(key);
        }
        else {
            return new CapacityRequirements();
        }
    }

    private void addAllInternal(AbstractCapacityRequirementsPerKey clusterCapacityRequirements) {
        for (String key : clusterCapacityRequirements.capacityPerKey.keySet()) {
            CapacityRequirements capacity = clusterCapacityRequirements.capacityPerKey.get(key);
            addInternal(key,capacity);
        }
    }
    
    private void subtractAllInternal(AbstractCapacityRequirementsPerKey aggregatedCapacity) {
        for (String key : aggregatedCapacity.capacityPerKey.keySet()) {
            CapacityRequirements capacity = aggregatedCapacity.capacityPerKey.get(key);
            subtractInternal(key,capacity);
        }
    }
    
    private void setInternal(String key, CapacityRequirements newCapacity) {

        CapacityRequirements oldCapacity = capacityPerKey.get(key);
        
        if (newCapacity.equalsZero()) {
            capacityPerKey.remove(key);
        }
        else {
            capacityPerKey.put(key,newCapacity);
        }
        
        if (oldCapacity != null) {
            totalCapacity = totalCapacity.subtract(oldCapacity);
        }
        
        totalCapacity = totalCapacity.add(newCapacity);
    
    }

    private void addInternal(String key, CapacityRequirements capacityToAdd) {
        
        validateAllocation(capacityToAdd);
        CapacityRequirements sumCapacity = capacityToAdd;
        if (capacityPerKey.containsKey(key)) {
            
            sumCapacity = sumCapacity.add(capacityPerKey.get(key));
        }
        
        capacityPerKey.put(key,sumCapacity);
        totalCapacity = totalCapacity.add(capacityToAdd);
    }

  
    private void subtractInternal(String key, CapacityRequirements capacity) {
        
        validateAllocation(capacity);
        
        if (!capacityPerKey.containsKey(key)) {
            throw new IllegalArgumentException("Agent UID " + key + " no found");
        }
        
        CapacityRequirements newAllocation = 
            capacityPerKey.get(key).subtract(capacity);
        
        updateKeyCapacity(key, newAllocation);
        
        totalCapacity = totalCapacity.subtract(capacity);
    }
    

    private void subtractOrZeroInternal(String key, CapacityRequirements capacity) {
   validateAllocation(capacity);
        
        if (!capacityPerKey.containsKey(key)) {
            throw new IllegalArgumentException("Agent UID " + key + " no found");
        }
        
        CapacityRequirements newAllocation = 
            capacityPerKey.get(key).subtractOrZero(capacity);
        
        updateKeyCapacity(key, newAllocation);
        totalCapacity = totalCapacity.subtract(capacity);
        
    }

    private void updateKeyCapacity(String key, CapacityRequirements newAllocation) {
        if (newAllocation.equalsZero()) {
            capacityPerKey.remove(key);
        }
        else {
            capacityPerKey.put(key,newAllocation);
        }
    }
    
    private void validateAllocation(CapacityRequirements allocation) {
        if (allocation.equalsZero()) {
            throw new IllegalArgumentException(allocation + " equals zero");
        }
    }
}
