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

import java.util.Collection;

public class CapacityRequirementsPerAgent extends AbstractCapacityRequirementsPerKey {

    public CapacityRequirementsPerAgent() {
    }
        
    public Collection<String> getAgentUids() {
        return super.getKeys();
    }
    
    @Override
    public CapacityRequirementsPerAgent set(String agentUid, CapacityRequirements capacity) {
        return (CapacityRequirementsPerAgent) super.set(agentUid, capacity);
    }

    @Override
    public CapacityRequirementsPerAgent add(
            String agentUid, 
            CapacityRequirements capacity) {
        
      return (CapacityRequirementsPerAgent) super.add(agentUid, capacity);
    }

    @Override
    public CapacityRequirementsPerAgent subtract(
            String agentUid, 
            CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerAgent) super.subtract(agentUid, capacity);
    }

    public CapacityRequirementsPerAgent add(CapacityRequirementsPerAgent other) {
        return (CapacityRequirementsPerAgent) super.add(other);
    }


    public CapacityRequirementsPerAgent subtract(CapacityRequirementsPerAgent other) {
        return (CapacityRequirementsPerAgent) super.subtract(other);
    }
    
    public CapacityRequirementsPerAgent subtractAgent(
            String agentUid) {
        return (CapacityRequirementsPerAgent) super.subtractKey(agentUid);
    }
    
    @Override
    public CapacityRequirementsPerAgent subtractOrZero(
           String agentUid, CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerAgent) super.subtractOrZero(agentUid, capacity);
    }


    public CapacityRequirements getAgentCapacity(String agentUid) {
        return super.getKeyCapacity(agentUid);
    }
    
    public CapacityRequirements getAgentCapacityOrZero(String agentUid) {
        return super.getKeyCapacityOrZero(agentUid);
    }

    @Override
    protected CapacityRequirementsPerAgent newZeroInstance() {
        return new CapacityRequirementsPerAgent();
    }

    @Override
    public String toString() {
        return super.getKeys().size() + " machines with total capacity of " + getTotalAllocatedCapacity();
    }
    
    @Override
    public String toDetailedString() {
        StringBuilder builder = new StringBuilder();
        builder.append("totalNumberOfMachines:" + super.getKeys().size() + " , totalCapacity:" + getTotalAllocatedCapacity()+", details:{");
        for (String key : super.getKeys()) {
            builder.append(key + ":" + super.getKeyCapacity(key)+" , ");
        }
        builder.append("}");
        return builder.toString();
    }
}
