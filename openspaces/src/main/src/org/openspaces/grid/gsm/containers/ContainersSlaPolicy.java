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
package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class ContainersSlaPolicy extends ServiceLevelAgreementPolicy {

    private GridServiceContainerConfig newContainerConfig;
    private CapacityRequirementsPerAgent clusterCapacityRequirements;
    
    public void setNewContainerConfig(GridServiceContainerConfig config) {
        this.newContainerConfig = config;
    }
    
    public GridServiceContainerConfig getNewContainerConfig() {
        return this.newContainerConfig;
    }
    
    public CapacityRequirementsPerAgent getClusterCapacityRequirements() {
        return this.clusterCapacityRequirements;
    }
    
    public void setClusterCapacityRequirements(CapacityRequirementsPerAgent clusterCapacityRequirements) {
        this.clusterCapacityRequirements = clusterCapacityRequirements;
    }

    public boolean isUndeploying() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clusterCapacityRequirements == null) ? 0 : clusterCapacityRequirements.hashCode());
        result = prime * result + ((newContainerConfig == null) ? 0 : newContainerConfig.hashCode());
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
        ContainersSlaPolicy other = (ContainersSlaPolicy) obj;
        if (clusterCapacityRequirements == null) {
            if (other.clusterCapacityRequirements != null)
                return false;
        } else if (!clusterCapacityRequirements.equals(other.clusterCapacityRequirements))
            return false;
        if (newContainerConfig == null) {
            if (other.newContainerConfig != null)
                return false;
        } else if (!newContainerConfig.equals(other.newContainerConfig))
            return false;
        return true;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        
        if (newContainerConfig == null) {
            throw new IllegalArgumentException("newContainerConfig cannot be null");
        }
        
        if (newContainerConfig.getMaximumMemoryCapacityInMB() <=0) {
            throw new IllegalArgumentException("newContainerConfig memory capacity cannot be zero.");
        }
        
        if (newContainerConfig.getMaximumJavaHeapSizeInMB() <=0) {
            throw new IllegalArgumentException("newContainerConfig memory capacity cannot be zero.");
        }
        
        if (clusterCapacityRequirements == null) {
            throw new IllegalArgumentException("clusterCapacityRequirements cannot be null");
        }
    }
}
