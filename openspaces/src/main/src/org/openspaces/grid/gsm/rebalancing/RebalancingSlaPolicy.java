package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RebalancingSlaPolicy extends ServiceLevelAgreementPolicy {

    private GridServiceContainer[] containers;
    
    public void setContainers(GridServiceContainer[] containers) {
        this.containers = containers;
    }
    public GridServiceContainer[] getContainers() {
        return containers;
    }
    
    public boolean equals(Object other) {
        return other instanceof ContainersSlaPolicy &&
        Arrays.asList(((RebalancingSlaPolicy)other).containers).equals(Arrays.asList(containers));
    }
}
