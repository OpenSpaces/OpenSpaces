package org.openspaces.grid.gsm.rebalancing;

import java.util.Arrays;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class RebalancingSlaPolicy extends ServiceLevelAgreementPolicy {

    
    private GridServiceContainer[] containers;
    private int maxNumberOfConcurrentRelocationsPerMachine;
    private ProcessingUnitSchemaConfig schema;
    private ClusterCapacityRequirements allocatedCapacity;
    
    public void setContainers(GridServiceContainer[] containers) {
        this.containers = containers;
    }
    
    public GridServiceContainer[] getContainers() {
        return containers;
    }
    
    public void setMaximumNumberOfConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        this.maxNumberOfConcurrentRelocationsPerMachine = maxNumberOfConcurrentRelocationsPerMachine;
    }
    
    public int getMaximumNumberOfConcurrentRelocationsPerMachine() {
        return maxNumberOfConcurrentRelocationsPerMachine;
    }

    public ProcessingUnitSchemaConfig getSchemaConfig() {
        return schema;
    }
    
    public void setSchemaConfig(ProcessingUnitSchemaConfig schemaConfig) {
        this.schema = schemaConfig;
    }
    
    public ClusterCapacityRequirements getAllocatedCapacity() {
        return allocatedCapacity;
    }
    
    public void setAllocatedCapacity(ClusterCapacityRequirements allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
    }
    
    public boolean equals(Object other) {
        return other instanceof RebalancingSlaPolicy &&
        ((RebalancingSlaPolicy)other).allocatedCapacity.equals(this.allocatedCapacity) &&
        ((RebalancingSlaPolicy)other).maxNumberOfConcurrentRelocationsPerMachine == maxNumberOfConcurrentRelocationsPerMachine &&
        ((RebalancingSlaPolicy)other).schema == schema &&
        Arrays.asList(((RebalancingSlaPolicy)other).containers).equals(Arrays.asList(containers));
    }
   
}
