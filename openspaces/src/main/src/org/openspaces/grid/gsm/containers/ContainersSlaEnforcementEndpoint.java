package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementEndpointDestroyedException;

/**
 * A service that on demand enforces the specified number of containers. 
 *  
 * @author itaif
 *
 * @see ContainersSlaPolicy
 */
public interface ContainersSlaEnforcementEndpoint 
    extends ServiceLevelAgreementEnforcementEndpoint<ContainersSlaPolicy> {
    
    /**
     * @return the list of containers managed by this service.
     * @throws SlaEnforcementEndpointDestroyedException 
     */
    GridServiceContainer[] getContainers() throws SlaEnforcementEndpointDestroyedException;

}
