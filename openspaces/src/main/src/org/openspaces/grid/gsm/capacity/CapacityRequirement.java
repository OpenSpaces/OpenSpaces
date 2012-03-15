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

/**
 * A machine capacity requirement for the {@link org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioning}
 * 
 * Each implementation must have a public default constructor that creates a zero capacity requirement object.
 * Each implementation must be immutable.
 * 
 * @author itaif
 * @see CapacityRequirements
 */
public interface CapacityRequirement extends Comparable<CapacityRequirement> {

    String toString();
    
    boolean equals(Object otherCapacityRequirement);

    boolean equalsZero();
    
    CapacityRequirement multiply(int i);
    
    CapacityRequirement divide(int numberOfContainers);
    
    CapacityRequirement subtract(CapacityRequirement otherCapacityRequirement);
    
    CapacityRequirement subtractOrZero(CapacityRequirement otherCapacityRequirement);
    
    CapacityRequirement add(CapacityRequirement otherCapacityRequirement);

    CapacityRequirement min(CapacityRequirement otherCapacityRequirement);

    CapacityRequirement max(CapacityRequirement otherCapacityRequirement);

    double divide(CapacityRequirement otherCapacityRequirement);

    CapacityRequirementType<? extends CapacityRequirement> getType();
}
