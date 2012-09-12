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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jini.rio.core.RequiredDependencies;
import org.jini.rio.core.RequiredDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

public abstract class AbstractProcessingUnitDetailedDependencies<T extends ProcessingUnitDependency, IT extends InternalProcessingUnitDependency> 
    implements InternalProcessingUnitDetailedDependencies<T,IT> {

    private final List<IT> deploymentDependencies;
    private final InternalProcessingUnitDependencyFactory<IT> dependencyFactory;
    
    public AbstractProcessingUnitDetailedDependencies(InternalProcessingUnitDependencyFactory<IT> dependencyFactory) {
        this.deploymentDependencies = new LinkedList<IT>();
        this.dependencyFactory = dependencyFactory;
    }
    
    @Override
    public boolean isEmpty() {
        return deploymentDependencies.isEmpty();
    }

    @Override
    public String[] getRequiredProcessingUnitsNames() {
        List<String> names = new ArrayList<String>(deploymentDependencies.size());
        
        for (IT dependency : deploymentDependencies) {
            names.add(dependency.getRequiredProcessingUnitName());
        }
        return names.toArray(new String[deploymentDependencies.size()]);
    }

    @SuppressWarnings("unchecked") // IT extends T
    @Override
    public void addDependency(T newDependency) {
        String requiredProcessingUnitName = newDependency.getRequiredProcessingUnitName();
        IT existingDependency = (IT) getDependencyByName(requiredProcessingUnitName);
        if (existingDependency != null) {
            existingDependency.mergeDependency(newDependency);
        }
        else {
            deploymentDependencies.add((IT)newDependency);
        }
    }

    @SuppressWarnings("unchecked") // IT extends T
    @Override
    public T getDependencyByName(String requiredProcessingUnitName) {
        for (IT dependency : deploymentDependencies) {
            if (requiredProcessingUnitName.equals(dependency.getRequiredProcessingUnitName())) {
                return (T)dependency;
            }
        }
        return null;
    }
    
    @Override
    public RequiredDependencies toRequiredDependencies() {
        
        RequiredDependencies requiredDependencies = new RequiredDependencies();
        for (IT dependency : deploymentDependencies){
            requiredDependencies.addRequiredDependency(dependency.toRequiredDependency());
        }
        return requiredDependencies;
    }

    @SuppressWarnings("unchecked") // IT extends T
    @Override
    public void addDependencies(RequiredDependencies requiredDependencies) {
        for (final String requiredDependencyName : requiredDependencies.getRequiredDependenciesNames()){
            final RequiredDependency requiredDependency = requiredDependencies.getRequiredDependencyByName(requiredDependencyName);
            final IT dependency = dependencyFactory.create(requiredDependency.getRequiredProcessingUnitName());
            dependency.mergeDependency(requiredDependency);
            addDependency((T)dependency);
        }
    }

}
