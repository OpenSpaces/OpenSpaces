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
 * Indicates that the processing unit requires dedicated machines.
 * It cannot be deployed on machines with other processing units.
 * 
 * @author itaif
 *
 */
public class DedicatedMachineIsolation extends ElasticProcessingUnitMachineIsolation {

    private final String puName;
    
    public DedicatedMachineIsolation(String puName) {
        if (puName == null || puName.length() == 0) {
            throw new IllegalArgumentException("puName");
        }
        this.puName = puName;
    }
    
    public String toString() {
        return "dedicated-isolation-"+puName;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof DedicatedMachineIsolation &&
               ((DedicatedMachineIsolation)other).puName.equals(this.puName);
    }
    
    @Override
    public int hashCode() {
         return puName.hashCode();
    }
}
