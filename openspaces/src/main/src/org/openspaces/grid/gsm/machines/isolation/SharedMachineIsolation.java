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
package org.openspaces.grid.gsm.machines.isolation;

/**
 * Indicates that the processing unit does not require dedicated machines.
 * It can be deployed on machines with other processing units that require shared machine isolation with the same sharingId.
 * It cannot be deployed with public processing units nor with shared processing units that have a different sharingId.
 * 
 * @author itaif
 *
 */
public class SharedMachineIsolation extends ElasticProcessingUnitMachineIsolation {

    private final String sharingId;

    public SharedMachineIsolation(String sharingId) {
        
        if (sharingId == null || sharingId.length() == 0) {
            throw new IllegalArgumentException("sharingId");
        }
        this.sharingId = sharingId;
    }
    
    public String toString() {
        return "shared-machine-isolation-" + sharingId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SharedMachineIsolation &&
               ((SharedMachineIsolation)other).sharingId.equals(sharingId);
    }
    
    @Override
    public int hashCode() {
        return sharingId.hashCode();
    }
}
