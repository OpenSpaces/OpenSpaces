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

public abstract class AbstractCapacityRequirement implements CapacityRequirement {

    protected final Long value;
    
    public AbstractCapacityRequirement() {
        this(0L);
    }
    
    public AbstractCapacityRequirement(long value) {
        this.value = value;
    }

    public int compareTo(CapacityRequirement otherCapacityRequirement) {
        
        AbstractCapacityRequirement other = cast(otherCapacityRequirement);
        return value.compareTo(other.value);
    }

    public boolean equalsZero() {
        return value == 0;
    }

    public CapacityRequirement multiply(int i) {
        return getType().newInstance(value*i);
    }

    public CapacityRequirement divide(int i) {
        return getType().newInstance(value/i);
    }

    public CapacityRequirement subtract(CapacityRequirement otherCapacityRequirement) {
        AbstractCapacityRequirement other = cast(otherCapacityRequirement);
        if (value < other.value) {
            throw new IllegalArgumentException("other size is " + other.value +" which is bigger than " + value);
        }
        return getType().newInstance(value - other.value);
    }

    public CapacityRequirement subtractOrZero(CapacityRequirement otherCapacityRequirement) {
        AbstractCapacityRequirement other = cast(otherCapacityRequirement);
        if (value < other.value) {
            return getType().newInstance(0L);
        }
        return getType().newInstance(value - other.value);
    }

    public CapacityRequirement add(CapacityRequirement otherCapacityRequirement) {
        AbstractCapacityRequirement other = cast(otherCapacityRequirement);
        return getType().newInstance(value + other.value);
    }

    public CapacityRequirement min(CapacityRequirement otherCapacityRequirement) {
        if (otherCapacityRequirement.compareTo(this) < 0) {
            return otherCapacityRequirement;
        }
        return this;
    }

    public CapacityRequirement max(CapacityRequirement otherCapacityRequirement) {
        if (otherCapacityRequirement.compareTo(this) > 0) {
            return otherCapacityRequirement;
        }
        return this;
    }

    public double divide(CapacityRequirement otherCapacityRequirement) {
        AbstractCapacityRequirement other = cast(otherCapacityRequirement);
        return ((double)value) / other.value;
    }

    public CapacityRequirementType<? extends AbstractCapacityRequirement> getType() {
        return new CapacityRequirementType<AbstractCapacityRequirement>(this.getClass());
    }
    
    public boolean equals(Object other) {
        return other instanceof AbstractCapacityRequirement &&
               ((AbstractCapacityRequirement)other).getType().equals(getType()) &&
               ((AbstractCapacityRequirement)other).value.equals(value);
    }
    
    public abstract String toString();
    
    private AbstractCapacityRequirement cast(Object o) {
        
        if (!getType().equals(((CapacityRequirement)o).getType())) {
            throw new IllegalArgumentException(((CapacityRequirement)o).getType() + " is not comparable with " + this.getType());
        }
        
        return ((AbstractCapacityRequirement)o);
    }

}
