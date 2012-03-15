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

import java.io.Externalizable;

import org.openspaces.admin.bean.BeanConfig;

public interface ScaleStrategyConfig extends BeanConfig , Externalizable {

    /**
     * Sets the polling interval in which the scale strategy SLA is monitored and enforced.
     * @param seconds - the polling interval in seconds
     */
    void setPollingIntervalSeconds(int seconds);
    
    int getPollingIntervalSeconds();
      
    int getMaxConcurrentRelocationsPerMachine();
    
    /**
     * Specifies the number of processing unit instance relocations each machine can handle concurrently.
     * Relocation requires network and CPU resources, and too many concurrent relocations per machine may degrade
     * its performance temporarily. The data recovery running as part of the relocation uses by default 4 threads.
     * So the total number of threads is 4 multiplied by the specified value.
     *  
     * By setting this value higher than 1, processing unit rebalancing
     * completes faster, by using more machine cpu and network resources.
     * 
     * Default value is 1.
     * 
     * This is an advanced property setting.
     * 
     * @param maxNumberOfConcurrentRelocationsPerMachine
     */
    void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine);
    
    boolean equals(Object other);
    int hashCode();
    String toString();
}
