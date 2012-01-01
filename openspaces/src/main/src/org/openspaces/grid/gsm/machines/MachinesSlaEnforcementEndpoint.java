package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;


/**
 * A service that on demand enforces the specified number of machines. 
 *  
 * @author itaif
 *
 * @see CapacityMachinesSlaPolicy
 */
public interface MachinesSlaEnforcementEndpoint extends ServiceLevelAgreementEnforcementEndpoint{
    
    void enforceSla(EagerMachinesSlaPolicy sla) throws GridServiceAgentSlaEnforcementInProgressException;
    
    void enforceSla(CapacityMachinesSlaPolicy sla) throws MachinesSlaEnforcementInProgressException, GridServiceAgentSlaEnforcementInProgressException;
    
    /**
     * @return a list of agents for this pu including memory/cpu for each.
     */
    ClusterCapacityRequirements getAllocatedCapacity();

}
