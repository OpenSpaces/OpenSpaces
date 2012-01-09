package org.openspaces.grid.gsm.strategy;

import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;

public interface DiscoveredMachinesCache {
    
    public Collection<GridServiceAgent> getDiscoveredAgents() throws MachinesSlaEnforcementInProgressException;
}
