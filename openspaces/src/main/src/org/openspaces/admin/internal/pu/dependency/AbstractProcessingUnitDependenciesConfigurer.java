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

import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

public abstract class AbstractProcessingUnitDependenciesConfigurer<
    T extends ProcessingUnitDependency, 
    IT extends InternalProcessingUnitDependency,
    S extends ProcessingUnitDetailedDependencies<T>,
    IS extends InternalProcessingUnitDetailedDependencies<T,IT>> {

    private final IS dependencies;
    private final InternalProcessingUnitDependencyFactory<IT> dependencyFactory;
    
    public AbstractProcessingUnitDependenciesConfigurer(InternalProcessingUnitDependencyFactory<IT> dependencyFactory, IS dependencies) {
        this.dependencyFactory = dependencyFactory;
        this.dependencies = dependencies;
    }
    
    @SuppressWarnings("unchecked") // IS extends S
    public S create() {
        return (S)dependencies;
    }

    public AbstractProcessingUnitDependenciesConfigurer<T,IT,S,IS> dependsOnMinimumNumberOfDeployedInstances(String requiredProcessingUnitName, int minimumNumberOfDeployedInstances) {
        
        IT dependency = createDependency(requiredProcessingUnitName);
        dependency.setMinimumNumberOfDeployedInstances(minimumNumberOfDeployedInstances);
        addDependency(dependency);
        return this;
    }

    public AbstractProcessingUnitDependenciesConfigurer<T,IT,S,IS> dependsOnMinimumNumberOfDeployedInstancesPerPartition(String requiredProcessingUnitName, int minimumNumberOfDeployedInstances) {
        
        IT dependency = createDependency(requiredProcessingUnitName);
        dependency.setMinimumNumberOfDeployedInstancesPerPartition(minimumNumberOfDeployedInstances);
        addDependency(dependency);
        return this;
    }

    public AbstractProcessingUnitDependenciesConfigurer<T,IT,S,IS> dependsOnDeployed(String requiredProcessingUnitName) {
        IT dependency = createDependency(requiredProcessingUnitName);
        dependency.setWaitForDeploymentToComplete(true);
        addDependency(dependency);
        return this;
    }

    protected IT createDependency(String requiredProcessingUnitName) {
        return dependencyFactory.create(requiredProcessingUnitName);
    }

    @SuppressWarnings("unchecked") // IT extends T
    protected void addDependency(IT dependency) {
        dependencies.addDependency((T)dependency);
    }
}
