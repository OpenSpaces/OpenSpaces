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
package org.openspaces.admin.internal.pu.dependency;

import org.jini.rio.core.RequiredDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

/**
 * 
 * @since 8.0.6
 * @author itaif
 */
public interface InternalProcessingUnitDependency extends ProcessingUnitDependency {

    /**
     * When true the dependant processing unit waits until the required processing unit completes deployment.
     * The deployment is considered complete when it's status is INTACT, and in case it is an Elastic Processing Unit then it also means scale is not in progress.
     * 
     * When false this dependency is disabled
     * @since 8.0.6
     */
    void setWaitForDeploymentToComplete(boolean waitForDeploymentToComplete);

    /**
     * When bigger than 0 the dependant processing unit waits until the required processing unit has at least the specified number of instances per partition.
     * 1 means at least one instance per partition (each partition has at least one primary instance)
     * 2 means at least two instances per partition (each partition has at least one primary instance and one backup instance)
     * 
     * When 0 this dependency is disabled
     * 
     * @since 8.0.6
     */
    void setMinimumNumberOfDeployedInstancesPerPartition(int minimumNumberOfDeployedInstancesPerPartition);
    
    /**
     * When bigger than 0 the dependant processing unit waits until the required processing unit has at least the specified number of instances.
     * When 0 this dependency is disabled
     * 
     * @since 8.0.6
     */
    void setMinimumNumberOfDeployedInstances(int minimumNumberOfDeployedInstances);
    
    /**
     * Merges the specified dependencies with existing dependencies.
     * 
     * @since 8.0.6
     */
    void mergeDependency(ProcessingUnitDependency otherDependency);
    
    /**
     * Merges the specified dependencies (received from the GSM) with existing dependencies.
     * 
     * @since 8.0.6
     */
    void mergeDependency(RequiredDependency otherRequiredDependency);

    /**
     * Converts this to the GSM implementation equivalent
     * 
     * @since 8.0.6
     */
    RequiredDependency toRequiredDependency();
        
}
