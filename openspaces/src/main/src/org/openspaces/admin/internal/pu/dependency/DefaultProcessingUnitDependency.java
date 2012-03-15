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

public class DefaultProcessingUnitDependency implements InternalProcessingUnitDependency {

    RequiredDependency requiredDependency;
    
    public DefaultProcessingUnitDependency(String requiredProcessingUnitName) {
        this(new RequiredDependency(requiredProcessingUnitName));
    }
    
    private DefaultProcessingUnitDependency(RequiredDependency requiredDependency) {
        this.requiredDependency = requiredDependency;
    }

    public boolean getWaitForDeploymentToComplete() {
        return this.requiredDependency.getWaitForDeploymentToComplete();
    }
    
    public void setWaitForDeploymentToComplete(boolean waitForDeploymentToComplete) {
        this.requiredDependency.setWaitForDeploymentToComplete(waitForDeploymentToComplete);
    }

    public int getMinimumNumberOfDeployedInstancesPerPartition() {
        return this.requiredDependency.getMinimumNumberOfDeployedInstancesPerPartition();
    }
    
    public void setMinimumNumberOfDeployedInstancesPerPartition(int minimumNumberOfDeployedInstancesPerPartition) {
        this.requiredDependency.setMinimumNumberOfDeployedInstancesPerPartition(minimumNumberOfDeployedInstancesPerPartition);
    }

    public int getMinimumNumberOfDeployedInstances() {
        return this.requiredDependency.getMinimumNumberOfDeployedInstances();
    }
    
    public void setMinimumNumberOfDeployedInstances(int minimumNumberOfDeployedInstances) {
        this.requiredDependency.setMinimumNumberOfDeployedInstances(minimumNumberOfDeployedInstances);
    }

    @Override
    public String getRequiredProcessingUnitName() {
        return this.requiredDependency.getRequiredProcessingUnitName();
    }

    @Override
    public void mergeDependency(ProcessingUnitDependency otherDependency) {
        mergeDependency(((InternalProcessingUnitDependency)otherDependency).toRequiredDependency());
    }

    @Override
    public void mergeDependency(RequiredDependency otherRequiredDependency) {
        requiredDependency.merge(otherRequiredDependency);
    }

    @Override
    public RequiredDependency toRequiredDependency() {
        return new RequiredDependency(requiredDependency);
    }

}
