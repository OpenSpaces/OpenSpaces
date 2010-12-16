package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;

public interface RebalancingSlaEnforcementEndpoint 
    extends ServiceLevelAgreementEnforcementEndpoint<ProcessingUnit,RebalancingSlaPolicy> {


}
