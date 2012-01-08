package org.openspaces.grid.gsm.containers;

import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;

public class UndeployContainersSlaPolicy extends ContainersSlaPolicy {
    
    public UndeployContainersSlaPolicy() {
        super();
        super.setClusterCapacityRequirements(new ClusterCapacityRequirements());
    }
    
    public boolean isUndeploying() {
        return true;
    }

}
