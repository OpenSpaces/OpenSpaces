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
 * Aggregates all of the processing unit lifecycle stages that can be postponed.
 * For XAP processing units it includes postponing of processing unit instance deployment.
 * Cloudify extends this interface to include also the start stage of the processing unit instance.  
 * 
 * @author itaif
 * @since 8.0.6
 * @param <T> The life cycle of the required processing unit. This is an extension point for Cloudify that has a more elaborate startup sequence and allows depending on different startup stages of the required processing unit.
 */
public interface ProcessingUnitDependencies<T extends ProcessingUnitDependency> {
    
    ProcessingUnitDeploymentDependencies<T> getDeploymentDependencies();
    
    /**
     * @return the names of all required processing units (All processing units that are dependent upon)
     */
    String[] getDependenciesRequiredProcessingUnitNames();
}
