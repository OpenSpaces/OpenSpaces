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
package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.core.util.MemoryUnit;

public interface ElasticStatefulDeploymentTopology extends ElasticDeploymentTopology , EagerScaleTopology , ManualCapacityScaleTopology{

    /**
     * Specifies an estimate of the maximum memory capacity for this processing unit.
     * The actual maximum memory capacity will be at least the specified maximum.
     * Requires the memoryCapacityPerContainer() property.
     * The memory capacity value is the sum of both the primary and backup instances memory capacity.
     */
    ElasticStatefulDeploymentTopology maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit);

    /**
     * Specifies an estimate of the minimum memory capacity for this processing unit.
     * The actual maximum memory capacity will be at least the specified maximum.
     * Requires the memoryCapacityPerContainer() property.
     * The memory capacity value is the sum of both the primary and backup instances memory capacity.
     */
    ElasticStatefulDeploymentTopology maxMemoryCapacity(String maxMemoryCapacity);

    /**
     * Specifies an estimate for the maximum total number of cpu cores used by this processing unit.
     */
    ElasticStatefulDeploymentTopology maxNumberOfCpuCores(int maxNumberOfCpuCores);
        
    /**
     * Specifies if the space should duplicate each information on two different machines.
     * If set to false then partition data is lost each time fail-over or scaling occurs.
     * By default highlyAvailable is true
     */
    ElasticStatefulDeploymentTopology highlyAvailable(boolean highlyAvailable);
    
 }
