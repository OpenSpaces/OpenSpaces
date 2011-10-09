package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.pu.ProcessingUnit;

public class ProcessingUnitIsNotInTactException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public ProcessingUnitIsNotInTactException(ProcessingUnit pu) {
        super(pu, "Deployment is not intact.");
    }

}
