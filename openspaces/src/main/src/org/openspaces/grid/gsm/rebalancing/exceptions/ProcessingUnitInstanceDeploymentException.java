package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.pu.ProcessingUnit;

@SuppressWarnings("serial")
class ProcessingUnitInstanceRelocationException extends RebalancingSlaEnforcementInProgressException{

    public ProcessingUnitInstanceRelocationException(ProcessingUnit pu) {
        super(pu);
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 2825472177059349288L;

}
