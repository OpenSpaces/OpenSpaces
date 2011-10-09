package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementEndpointDestroyedException;


/**
 * A service that on demand enforces the specified number of machines. 
 *  
 * @author itaif
 *
 * @see CapacityMachinesSlaPolicy
 */
public interface MachinesSlaEnforcementEndpoint 
    extends ServiceLevelAgreementEnforcementEndpoint<CapacityMachinesSlaPolicy> {
    
    /**
     * @return a list of agents for this pu including memory/cpu for each.
     */
    ClusterCapacityRequirements getAllocatedCapacity() throws SlaEnforcementEndpointDestroyedException;

}
