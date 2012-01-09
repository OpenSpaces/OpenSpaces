package org.openspaces.grid.gsm.machines;

import java.util.Collection;
import java.util.HashSet;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.strategy.DiscoveredMachinesCache;

public class UndeployMachinesSlaPolicy extends CapacityMachinesSlaPolicy {

    final DiscoveredMachinesCache robustMachinesCache;
    
    public UndeployMachinesSlaPolicy(final Admin admin) {
        super();
        super.setMinimumNumberOfMachines(0);
        super.setCapacityRequirements(new CapacityRequirements());
        
        this.robustMachinesCache = new DiscoveredMachinesCache() {

            @Override
            public Collection<GridServiceAgent> getDiscoveredAgents() {
                try {
                    return UndeployMachinesSlaPolicy.super.getDiscoveredMachinesCache().getDiscoveredAgents();
                } catch (MachinesSlaEnforcementInProgressException e) {
                    final Collection<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
                    for (final GridServiceAgent agent : admin.getGridServiceAgents().getAgents()) {
                        if (MachinesSlaUtils.isAgentConformsToMachineProvisioningConfig(agent, getMachineProvisioning().getConfig())) {
                            agents.add(agent);
                        }
                    }
                    return agents;
                }
            }
        };
    }
    
    @Override
    public boolean isUndeploying() {
        return true;
    }
    
    @Override
    public String getScaleStrategyName() {
        return "Undeploy Capacity Scale Strategy";
    }

    @Override
    public DiscoveredMachinesCache getDiscoveredMachinesCache() {
        return robustMachinesCache;
    }
}
