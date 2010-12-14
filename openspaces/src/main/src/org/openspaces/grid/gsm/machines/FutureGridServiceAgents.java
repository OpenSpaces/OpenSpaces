package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.core.PollingFuture;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

public interface FutureGridServiceAgents extends PollingFuture<GridServiceAgent[]> {
    
    CapacityRequirements getCapacityRequirements();
   
}
