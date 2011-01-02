package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RebalancingSlaPolicy extends ServiceLevelAgreementPolicy {

    private GridServiceContainer[] containers;
    private int maxNumberOfConcurrentRelocationsPerMachine;
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
    
    public boolean equals(Object other) {
        return other instanceof RebalancingSlaPolicy &&
        ((RebalancingSlaPolicy)other).maxNumberOfConcurrentRelocationsPerMachine == maxNumberOfConcurrentRelocationsPerMachine &&
        Arrays.asList(((RebalancingSlaPolicy)other).containers).equals(Arrays.asList(containers));
    }
    
}
