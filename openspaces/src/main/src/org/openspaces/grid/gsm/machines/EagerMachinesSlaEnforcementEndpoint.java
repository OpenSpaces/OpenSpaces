package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementEndpointDestroyedException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementException;

public interface EagerMachinesSlaEnforcementEndpoint {

    /**
     * 
     * Enforces the specified SLA without blocking the calling thread.
     * To use this method call enforceSla method until it returns true. 
     * Dynamic changes in the sla are reflected by specifying a different sla parameter each call.
     * 
     * @param sla - the sla parameters or null if the sla is to be cleared.
     * @return true if the sla was reached (steady state).
     */
    void enforceSla(EagerMachinesSlaPolicy sla) throws SlaEnforcementException;

    /**
     * @return a list of agents for this pu including memory/cpu for each.
     */
    ClusterCapacityRequirements getAllocatedCapacity() throws SlaEnforcementEndpointDestroyedException;

}
