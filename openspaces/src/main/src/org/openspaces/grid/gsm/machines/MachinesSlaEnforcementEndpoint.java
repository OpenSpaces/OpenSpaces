package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;


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
    AggregatedAllocatedCapacity getAllocatedCapacity() throws ServiceLevelAgreementEnforcementEndpointDestroyedException;

    /**
     * @return true if there are agents waiting to be cleared from containers.
     */
    boolean isGridServiceAgentsPendingDeallocation() throws ServiceLevelAgreementEnforcementEndpointDestroyedException;
}
