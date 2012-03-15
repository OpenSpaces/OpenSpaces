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





public interface AdvancedStatefulDeploymentTopology {

    /**
     * Overrides the number of backup processing unit instances per partition.
     * 
     * Default is 1
     * 
     * This is an advanced property.
     * 
     * @since 8.0 
     */
    public AdvancedStatefulDeploymentTopology numberOfBackupsPerPartition(int numberOfBackupsPerPartition);
    
    /**
     * Defines the number of processing unit partitions.
     * 
     * This property cannot be used with {@link #maxMemoryCapacity(String)} and {@link #maxNumberOfCpuCores(int)}.
     * 
     * This is an advanced property.
     * 
     * @since 8.0
     */
    public AdvancedStatefulDeploymentTopology numberOfPartitions(int numberOfPartitions);

    /**
     * Allows deployment of the processing unit on a single machine, by lifting the limitation
     * for primary and backup processing unit instances from the same partition to be deployed on different machines.
     * Default value is false (by default primary instances and backup instances need separate machines).
     * 
     * This is an advanced property.
     * 
     * @since 8.0
     */
    public AdvancedStatefulDeploymentTopology singleMachineDeployment();
   
}
