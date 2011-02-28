package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.core.PollingFuture;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;

public interface FutureGridServiceAgent extends PollingFuture<GridServiceAgent> {
    
    NonBlockingElasticMachineProvisioning getMachineProvisioning();
    
    CapacityRequirements getFutureCapacity();
   
}
