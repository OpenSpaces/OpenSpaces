package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;


/**
 * A service that on demand enforces the specified number of machines. 
 *  
 * @author itaif
 *
 * @see MachineSlaPolicy
 */
public interface MachinesSlaEnforcementEndpoint 
    extends ServiceLevelAgreementEnforcementEndpoint<String,MachinesSlaPolicy> {
    
    /**
     * @return a list of agents for this zone that are not pending shutdown, without blocking the calling thread.
     */
    GridServiceAgent[] getGridServiceAgents() throws ServiceLevelAgreementEnforcementEndpointDestroyedException;

    /**
     * @return a list of agents for this zone that that need to be cleared of containers, without blocking the calling thread.
     * Unless all containers are killed the sla would not be reached.
     */
    GridServiceAgent[] getGridServiceAgentsPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException;
}
