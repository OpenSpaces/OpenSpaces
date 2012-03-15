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

public interface MaxNumberOfContainersScaleConfig {

    /**
     * Defines a maximum for the total number of containers.  
     * 
     * For example in the following scenario, given the maximum is 3,
     * a new container is not started even if a scale out trigger is relevant. 
     * 
     * Machine A: GSC GSC 
     * Machine B: GSC
     * 
     */
    public void setMaxNumberOfContainers(int numberOfContainers);
    
    public int getMaxNumberOfContainers();
}
