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
package org.openspaces.admin.pu.dependency.config;

import org.jini.rio.core.RequiredDependency;
import org.openspaces.admin.config.XmlProperty;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.springframework.beans.factory.annotation.Required;

public class ProcessingUnitDependencyConfig implements InternalProcessingUnitDependency {

    private boolean waitForDeploymentToComplete;
    private int minimumNumberOfDeployedInstancesPerPartition;
    private int minimumNumberOfDeployedInstances;
    private String requiredProcessingUnitName;    
    
    public boolean getWaitForDeploymentToComplete() {
        return waitForDeploymentToComplete;
    }

    @XmlProperty
    public void setWaitForDeploymentToComplete(boolean waitForDeploymentToComplete) {
        this.waitForDeploymentToComplete = waitForDeploymentToComplete;
    }

    public int getMinimumNumberOfDeployedInstancesPerPartition() {
        return minimumNumberOfDeployedInstancesPerPartition;
    }

    @XmlProperty
    public void setMinimumNumberOfDeployedInstancesPerPartition(int minimumNumberOfDeployedInstancesPerPartition) {
        this.minimumNumberOfDeployedInstancesPerPartition = minimumNumberOfDeployedInstancesPerPartition;
    }

    public int getMinimumNumberOfDeployedInstances() {
        return minimumNumberOfDeployedInstances;
    }

    @XmlProperty
    public void setMinimumNumberOfDeployedInstances(int minimumNumberOfDeployedInstances) {
        this.minimumNumberOfDeployedInstances = minimumNumberOfDeployedInstances;
    }

    public String getRequiredProcessingUnitName() {
        return requiredProcessingUnitName;
    }

    private void init(RequiredDependency requiredDependency) {
        this.requiredProcessingUnitName = requiredDependency.getRequiredProcessingUnitName();
        this.minimumNumberOfDeployedInstances = requiredDependency.getMinimumNumberOfDeployedInstances();
        this.minimumNumberOfDeployedInstancesPerPartition = requiredDependency.getMinimumNumberOfDeployedInstancesPerPartition();
        this.waitForDeploymentToComplete = requiredDependency.getWaitForDeploymentToComplete();
    }
    
    @Required
    @XmlProperty
    public void setRequiredProcessingUnitName(String requiredProcessingUnitName) {
        this.requiredProcessingUnitName = requiredProcessingUnitName;
    }

    /**
     * For spring injection
     */
    public ProcessingUnitDependencyConfig() {
    }

    @Override
    public void mergeDependency(ProcessingUnitDependency otherDependency) {
        mergeDependency(((InternalProcessingUnitDependency)otherDependency).toRequiredDependency());
    }

    @Override
    public void mergeDependency(RequiredDependency otherRequiredDependency) {
        RequiredDependency requiredDependency = toRequiredDependency();
        requiredDependency.merge(otherRequiredDependency);
        init(requiredDependency);
    }
    
    
    @Override
    public RequiredDependency toRequiredDependency() {
        RequiredDependency requiredDependency = new RequiredDependency(requiredProcessingUnitName);
        requiredDependency.setMinimumNumberOfDeployedInstances(minimumNumberOfDeployedInstances);
        requiredDependency.setMinimumNumberOfDeployedInstancesPerPartition(minimumNumberOfDeployedInstancesPerPartition);
        requiredDependency.setWaitForDeploymentToComplete(waitForDeploymentToComplete);
        return requiredDependency;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + minimumNumberOfDeployedInstances;
        result = prime * result + minimumNumberOfDeployedInstancesPerPartition;
        result = prime * result + ((requiredProcessingUnitName == null) ? 0 : requiredProcessingUnitName.hashCode());
        result = prime * result + (waitForDeploymentToComplete ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessingUnitDependencyConfig other = (ProcessingUnitDependencyConfig) obj;
        if (minimumNumberOfDeployedInstances != other.minimumNumberOfDeployedInstances)
            return false;
        if (minimumNumberOfDeployedInstancesPerPartition != other.minimumNumberOfDeployedInstancesPerPartition)
            return false;
        if (requiredProcessingUnitName == null) {
            if (other.requiredProcessingUnitName != null)
                return false;
        } else if (!requiredProcessingUnitName.equals(other.requiredProcessingUnitName))
            return false;
        if (waitForDeploymentToComplete != other.waitForDeploymentToComplete)
            return false;
        return true;
    }
}
