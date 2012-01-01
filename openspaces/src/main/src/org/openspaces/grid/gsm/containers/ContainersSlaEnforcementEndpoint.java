package org.openspaces.grid.gsm.containers;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;

/**
 * A service that on demand enforces the specified number of containers. 
 *  
 * @author itaif
 *
 * @see ContainersSlaPolicy
 */
public interface ContainersSlaEnforcementEndpoint extends ServiceLevelAgreementEnforcementEndpoint{
    
    void enforceSla(ContainersSlaPolicy sla) throws ContainersSlaEnforcementInProgressException;
    
    /**
     * @return the list of containers managed by this service.
     * @throws SlaEnforcementEndpointDestroyedException 
     */
    GridServiceContainer[] getContainers() ;

}
