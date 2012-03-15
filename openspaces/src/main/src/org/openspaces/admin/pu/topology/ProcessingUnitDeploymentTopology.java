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
package org.openspaces.admin.pu.topology;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependenciesConfigurer;

import com.gigaspaces.security.directory.UserDetails;

public interface ProcessingUnitDeploymentTopology {
    
    /**
     * Will deploy a secured processing unit. Note, by setting user details the processing unit will be secured automatically.
     */
    ProcessingUnitDeploymentTopology secured(boolean secured);

    /**
     * Advanced: Sets the security user details for authentication and authorization of the
     * processing unit.
     */
    ProcessingUnitDeploymentTopology userDetails(UserDetails userDetails);

    /**
     * Advanced: Sets the security user details for authentication and authorization of the
     * processing unit.
     */ 
    ProcessingUnitDeploymentTopology userDetails(String userName, String password);

    /**
     * Postpones deployment of processing unit instances until the specified dependencies are met.
     * 
     * The following example postpones the deployment of this processing unit until B has completed the deployment and C has at least one instance deployed.
     * deployment.addDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployment("B").dependsOnMinimumNumberOfDeployedInstances("C",1).create())
     * 
     * @see ProcessingUnitDeploymentDependenciesConfigurer
     * @since 8.0.6
     */
    ProcessingUnitDeploymentTopology addDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> deploymentDependencies);

    /**
     * Postpones deployment of processing unit instances deployment until the specified processing unit deployment is complete.
     * 
     * Same as: deployment.addDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployment(requiredProcessingUnitName).create())
     * @since 8.0.6
     */
    ProcessingUnitDeploymentTopology addDependency(String requiredProcessingUnitName);
    
    /**
     * Converts the deployment to a standard {@link ProcessingUnitDeployment} 
     */
    ProcessingUnitDeployment toProcessingUnitDeployment(Admin admin);
    
}
