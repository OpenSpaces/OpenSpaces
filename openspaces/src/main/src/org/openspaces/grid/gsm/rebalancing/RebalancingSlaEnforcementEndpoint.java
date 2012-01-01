package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;

public interface RebalancingSlaEnforcementEndpoint extends ServiceLevelAgreementEnforcementEndpoint{
    
    void enforceSla(RebalancingSlaPolicy sla) throws RebalancingSlaEnforcementInProgressException;

}
