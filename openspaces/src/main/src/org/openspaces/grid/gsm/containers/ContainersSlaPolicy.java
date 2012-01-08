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
