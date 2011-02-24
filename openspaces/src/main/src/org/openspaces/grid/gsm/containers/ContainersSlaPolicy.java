package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class ContainersSlaPolicy extends ServiceLevelAgreementPolicy {

    private GridServiceContainerConfig newContainerConfig;
    private AggregatedAllocatedCapacity aggregatedAllocatedCapacity;
    
    public void setNewContainerConfig(GridServiceContainerConfig config) {
        this.newContainerConfig = config;
    }
    
    public GridServiceContainerConfig getNewContainerConfig() {
        return this.newContainerConfig;
    }
    
    public AggregatedAllocatedCapacity getAllocatedCapacity() {
        return this.aggregatedAllocatedCapacity;
    }
    
    public void setAllocatedCapacity(AggregatedAllocatedCapacity aggregatedAllocatedCapacity) {
        this.aggregatedAllocatedCapacity = aggregatedAllocatedCapacity;
    }
    
    public boolean equals(Object other) {
        return other instanceof ContainersSlaPolicy &&
               ((ContainersSlaPolicy)other).newContainerConfig.equals(this.newContainerConfig) &&
               ((ContainersSlaPolicy)other).aggregatedAllocatedCapacity.equals(this.aggregatedAllocatedCapacity);
    }

}
