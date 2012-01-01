package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.pu.ProcessingUnit;

public class ElasticProcessingUnitInstanceUndeployInProgress extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public ElasticProcessingUnitInstanceUndeployInProgress(ProcessingUnit pu) {
        super(pu, "Undeployment of "+ pu.getName() + " instances is still in progress");
    }

}
