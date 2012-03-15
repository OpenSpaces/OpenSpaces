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
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class ContainersSlaPolicy extends ServiceLevelAgreementPolicy {

    private GridServiceContainerConfig newContainerConfig;
    private ClusterCapacityRequirements clusterCapacityRequirements;
    
    public void setNewContainerConfig(GridServiceContainerConfig config) {
        this.newContainerConfig = config;
    }
    
    public GridServiceContainerConfig getNewContainerConfig() {
        return this.newContainerConfig;
    }
    
    public ClusterCapacityRequirements getClusterCapacityRequirements() {
        return this.clusterCapacityRequirements;
    }
    
    public void setClusterCapacityRequirements(ClusterCapacityRequirements clusterCapacityRequirements) {
        this.clusterCapacityRequirements = clusterCapacityRequirements;
    }

    public boolean isUndeploying() {
        return false;
    }

    public boolean equals(Object other) {
        return other instanceof ContainersSlaPolicy &&
               ((ContainersSlaPolicy)other).newContainerConfig.equals(this.newContainerConfig) &&
               ((ContainersSlaPolicy)other).clusterCapacityRequirements.equals(this.clusterCapacityRequirements);
    }

}
