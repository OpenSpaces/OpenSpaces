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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.gigaspaces.internal.utils.CollectionUtils;
import com.gigaspaces.internal.utils.StringUtils;

public class CapacityRequirements {

	private final CapacityRequirement[] requirements;
	
	public CapacityRequirements(CapacityRequirement... requirements) {
	   
	    // filter out zero requirements
	    List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        for (CapacityRequirement capacityRequirement : requirements) {
            if (!capacityRequirement.equalsZero()) {
                newRequirements.add(capacityRequirement);
            }
        }
        this.requirements = newRequirements.toArray(new CapacityRequirement[newRequirements.size()]);
	}
	
	public CapacityRequirement[] getRequirements() {
	    return this.requirements;
	}
	
	@SuppressWarnings("unchecked")
    public <T extends CapacityRequirement> T getRequirement(CapacityRequirementType<T> type) {
	    T requirement = null;
		
		for (CapacityRequirement r : requirements) {
			if (r.getType().equals(type)) {
				requirement = (T)r;
				break;
			}
		}
		
		if (requirement == null) {
			requirement = type.newInstance();
		}
		
		return requirement;
	}
	
	public CapacityRequirements multiply(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("i must not be negative");
        }
        CapacityRequirement[] newRequirements = new CapacityRequirement[this.requirements.length];
        for (int j = 0 ; j < newRequirements.length ; j++) {
            newRequirements[j] = this.requirements[j].multiply(i);
        }
        return new CapacityRequirements(newRequirements);
    }
	

    public CapacityRequirements divide(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("i must not be negative");
        }
        CapacityRequirement[] newCapacity = new CapacityRequirement[this.requirements.length];
        for (int j = 0 ; j < newCapacity.length ; j++) {
            newCapacity[j] = this.requirements[j].divide(i);
        }
        return new CapacityRequirements(newCapacity);
    }
    
    public CapacityRequirements subtract(CapacityRequirements otherRequirements) {
        if (otherRequirements == null) {
            throw new IllegalArgumentException("otherRequirements cannot be null");
        }
        List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        if (!this.greaterOrEquals(otherRequirements)) {
                throw new IllegalArgumentException("Cannot subtract " + otherRequirements + " from " + this +" since it would result in a negative capacity");
        }

        for (CapacityRequirement capacity : this.requirements) {
            CapacityRequirement otherCapacity = otherRequirements.getRequirement(capacity.getType());
            CapacityRequirement newCapacity = capacity.subtract(otherCapacity);
            if (!newCapacity.equalsZero()) {
                newRequirements.add(newCapacity);
            }
        }
        
        return new CapacityRequirements(newRequirements.toArray(new CapacityRequirement[newRequirements.size()]));
    }

    public CapacityRequirements subtract(CapacityRequirement otherRequirement) {
        return subtract(new CapacityRequirements(otherRequirement));
    }
    
    public CapacityRequirements subtractOrZero(CapacityRequirement otherRequirement) {
        return subtractOrZero(new CapacityRequirements(otherRequirement));
    }

    public CapacityRequirements subtractOrZero(CapacityRequirements otherRequirements) {
        
        List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        
        for (CapacityRequirement capacity : this.requirements) {
            CapacityRequirement otherCapacity = otherRequirements.getRequirement(capacity.getType());
            CapacityRequirement newCapacity = capacity.subtractOrZero(otherCapacity);
            if (!newCapacity.equalsZero()) {
                newRequirements.add(newCapacity);
            }
        }
        
        return new CapacityRequirements(newRequirements.toArray(new CapacityRequirement[newRequirements.size()]));
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(requirements);
        return result;
    }

    public boolean equals(Object otherRequirements) {
        
        boolean eq = true;
        
        if (!(otherRequirements instanceof CapacityRequirements)) {
            eq = false; 
        }
        else {
            for (CapacityRequirement otherCapacity : ((CapacityRequirements)otherRequirements).requirements) {
                CapacityRequirement capacity = this.getRequirement(otherCapacity.getType());
                if (capacity.compareTo(otherCapacity) != 0) {
                    eq = false;
                    break;
                }
            }

            for (CapacityRequirement capacity : this.requirements) {
                CapacityRequirement otherCapacity = ((CapacityRequirements)otherRequirements).getRequirement(capacity.getType());
                if (capacity.compareTo(otherCapacity) != 0) {
                    eq = false;
                    break;
                }
            }

        }
        
        return eq;
    }
  
    public boolean equalsZero() {

        boolean eq = true;
        
        for (CapacityRequirement capacity : this.requirements) {
            if (!capacity.equalsZero()) {
                eq = false;
                break;
            }
        }
        return eq;
    }

    public CapacityRequirements add(CapacityRequirements otherRequirements) {
    
        List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        
        for (CapacityRequirement otherCapacity : otherRequirements.requirements) {
            if (otherCapacity.equalsZero()) {
                throw new IllegalStateException(otherCapacity + " has a zero capacity requirement");
            }
            CapacityRequirement capacity = this.getRequirement(otherCapacity.getType());
            if (capacity.equalsZero()) {
                newRequirements.add(otherCapacity);
            }
        }

        for (CapacityRequirement capacity : this.requirements) {
            CapacityRequirement otherCapacity = otherRequirements.getRequirement(capacity.getType());
            CapacityRequirement newCapacity = capacity.add(otherCapacity);
            if (!newCapacity.equalsZero()) {
                newRequirements.add(newCapacity);
            }
        }
        
        return new CapacityRequirements(newRequirements.toArray(new CapacityRequirement[newRequirements.size()]));
    }

    public CapacityRequirements min(CapacityRequirements otherRequirements) {

        List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        
        for (CapacityRequirement capacity : this.requirements) {
            CapacityRequirement otherCapacity = otherRequirements.getRequirement(capacity.getType());
            if (capacity.compareTo(otherCapacity) > 0) {
                newRequirements.add(otherCapacity);
            }
            else {
                newRequirements.add(capacity);
            }
        }
        
        return new CapacityRequirements(newRequirements.toArray(new CapacityRequirement[newRequirements.size()]));
    }

    public CapacityRequirements max(CapacityRequirements otherCapacityRequirements) {
        List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        
        for (CapacityRequirement otherCapacityRequirement : otherCapacityRequirements.getRequirements()) {
            CapacityRequirement requirement = this.getRequirement(otherCapacityRequirement.getType());
            newRequirements.add(requirement.max(otherCapacityRequirement));
        }
        
        for (CapacityRequirement requirement : this.getRequirements()) {
            if (otherCapacityRequirements.getRequirement(requirement.getType()).equalsZero()) {
                newRequirements.add(requirement);
            }
        }
        
        return new CapacityRequirements(newRequirements.toArray(new CapacityRequirement[newRequirements.size()]));
    }
    
    public CapacityRequirements max(CapacityRequirement capacityRequirement) {
        
        if (getRequirement(capacityRequirement.getType()).compareTo(capacityRequirement) > 0) {
            return set(capacityRequirement);
        }
        else {
            return this;
        }
    }

    public CapacityRequirements set(CapacityRequirement capacityRequirement) {
        List<CapacityRequirement> newRequirements = new ArrayList<CapacityRequirement>();
        newRequirements.add(capacityRequirement);
        for (CapacityRequirement capacity : this.requirements) {
            if (!capacityRequirement.getType().equals(capacity.getType())) {
                newRequirements.add(capacity);
            }
        }
        
        return new CapacityRequirements(newRequirements.toArray(new CapacityRequirement[newRequirements.size()]));
    }

    /**
     * Divides this by the specified object. 
     * @return the exact integer that is the result of the division, or -1 if such integer for all requirements does not exist
     */
    public int divideExactly(CapacityRequirements otherCapacityRequirements) {
        int exactFactor = -1; // exact int division factor was not found
        if (!this.equalsZero() && !otherCapacityRequirements.equalsZero()) {
            CapacityRequirement thisRequirement = requirements[0];
            CapacityRequirement otherRequirement = otherCapacityRequirements.getRequirement(thisRequirement.getType());
            if (!otherRequirement.equalsZero()) {
                double factor =  thisRequirement.divide(otherRequirement);
                if (Math.round(factor) == factor) { 
                    // we found a possible exact division factor based on the first requirement
                    if (otherCapacityRequirements.equals(this.divide((int)factor))) {
                        // but this factor is not good for all requirements
                        exactFactor = (int) factor ;
                    }
                }
            }
        }
        return exactFactor;
    }

    public boolean greaterOrEquals(CapacityRequirements otherRequirements) {
        
        boolean greaterOrEquals = true;
        for (CapacityRequirement otherCapacity : otherRequirements.requirements) {
            CapacityRequirement capacity = this.getRequirement(otherCapacity.getType());
            if (capacity.compareTo(otherCapacity) < 0) {
                greaterOrEquals = false;
                break;
            }
        }
        return greaterOrEquals;
    }


    public boolean greaterThan(CapacityRequirements otherRequirements) {
        return greaterOrEquals(otherRequirements) && !equals(otherRequirements);
    }
    
    public String toString() {
        List<CapacityRequirement> sortedRequirements = 
            CollectionUtils.toList(requirements);
        Collections.sort(sortedRequirements, new Comparator<CapacityRequirement>() {

            public int compare(CapacityRequirement o1, CapacityRequirement o2) {
                String s1 = o1.getType().toString();
                String s2 = o2.getType().toString();
                return s1.compareTo(s2);
            }
        });
        if (sortedRequirements.isEmpty()) {
            sortedRequirements.add(new MemoryCapacityRequirement());
        }
        return StringUtils.arrayToCommaDelimitedString(
                sortedRequirements.toArray(new CapacityRequirement[sortedRequirements.size()]));
    }

    public CapacityRequirements add(CapacityRequirement capacityToAllocateOnMachine) {
        return add(new CapacityRequirements(capacityToAllocateOnMachine));
    }
}
