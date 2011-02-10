package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public interface EagerMachinesSlaEnforcementEndpoint {

    /**
     * 
     * Enforces the specified SLA without blocking the calling thread.
     * To use this method call enforceSla method until it returns true. 
     * Dynamic changes in the sla are reflected by specifying a different sla parameter each call.
     * 
     * @param sla - the sla parameters or null if the sla is to be cleared.
     * @return true if the sla was reached (steady state).
     * @throws ServiceLevelAgreementEnforcementEndpointDestroyedException - this object has already been destroyed
     */
    boolean enforceSla(EagerMachinesSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException;

    /**
     * @return a list of agents for this pu including memory/cpu for each.
     */
    AggregatedAllocatedCapacity getAllocatedCapacity() throws ServiceLevelAgreementEnforcementEndpointDestroyedException;

}
