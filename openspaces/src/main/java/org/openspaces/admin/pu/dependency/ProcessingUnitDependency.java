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
package org.openspaces.admin.pu.dependency;

/**
 * Defines dependency between an unspecified dependant processing unit and one required processing unit.
 * 
 * @author itaif
 * @since 8.0.6
 */
public interface ProcessingUnitDependency {

    /**
     * @return the name of the required processing unit (the processing unit that is being dependent-upon)
     * @since 8.0.6
     */
    String getRequiredProcessingUnitName();
    
    /**
     * @return true if the dependent processing unit waits until the required processing unit deployment is complete. False indicates this dependency is disabled.
     * @since 8.0.6
     */
    boolean getWaitForDeploymentToComplete();
    
    /**
     * @return the number of required processing unit instances per partition that the dependent processing unit waits for. Zero indicates this dependency is disabled.
     * @since 8.0.6
     */
    int getMinimumNumberOfDeployedInstancesPerPartition();
    
    /**
     * @return the number of required processing unit instances that the dependent processing unit waits for. Zero indicates this dependency is disabled.
     * @since 8.0.6
     */
    int getMinimumNumberOfDeployedInstances();
   
}
