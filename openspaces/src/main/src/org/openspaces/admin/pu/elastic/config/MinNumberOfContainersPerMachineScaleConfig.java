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
package org.openspaces.admin.pu.elastic.config;

public interface MinNumberOfContainersPerMachineScaleConfig {

    /**
     * Starts at least the specified number of containers per machine.
     * 
     * For example in the following scenario, given minimum per machine is 3,
     * and maximum per machine is 4, 
     * when machine C is started, 3 GSCs will be started on it.
     * 
     * Machine A: GSC GSC GSC GSC
     * Machine B: GSC GSC GSC
     * 
     */
    public void setMinNumberOfContainersPerMachine(int numberOfContainers);
    
    public int getMinNumberOfContainersPerMachine();
}
