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

public class CapacityRequirementsPerAgent extends AbstractCapacityRequirementsPerKey<CapacityRequirementsPerAgent> {

    public CapacityRequirementsPerAgent() {
    }
        
    public Collection<String> getAgentUids() {
        return super.getKeys();
    }
        
    public CapacityRequirementsPerAgent set(String agentUid, CapacityRequirements capacity) {
        return super.set(agentUid, capacity);
    }
    
    public CapacityRequirementsPerAgent add(
            String agentUid, 
            CapacityRequirements capacity) {
        
      return super.add(agentUid, capacity);
    }
    
    public CapacityRequirementsPerAgent subtract(
            String agentUid, 
            CapacityRequirements capacity) {
        
        return super.subtract(agentUid, capacity);
    }



    public CapacityRequirementsPerAgent subtractAgent(
            String agentUid) {
        return super.subtractKey(agentUid);
    }
    
    public CapacityRequirementsPerAgent subtractOrZero(
           String agentUid, CapacityRequirements capacity) {
        
        return super.subtractOrZero(agentUid, capacity);
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
}
