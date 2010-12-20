package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

/**
 * STUB - creates the same endpoint for any PU. Does not rebalance.
 * @author itaif
 *
 */
public class RebalancingSlaEnforcement 
    implements ServiceLevelAgreementEnforcement<RebalancingSlaPolicy, ProcessingUnit, RebalancingSlaEnforcementEndpoint>{

    private boolean destroyed;

    public RebalancingSlaEnforcement(Admin admin) {
       
    }

    public void destroy() {
        destroyed = true;
    }
    
    public boolean isBalanced(Object client, RebalancingSlaPolicy config) {
        return false;
    }

    public RebalancingSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {

        return new RebalancingSlaEnforcementEndpoint() {
            
            
            public ProcessingUnit getId() {
               return pu;
            }
            
            public boolean enforceSla(RebalancingSlaPolicy sla)
                    throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
                if (destroyed) {
                    throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
                }
                return false;
            }
        };
    }

    public void destroyEndpoint(ProcessingUnit id) {
        destroyed = true;
    }
}
