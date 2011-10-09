package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.pu.ProcessingUnit;

public class ProcessingUnitIsNotEvenlyDistributedAccrossMachinesException extends
        RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public ProcessingUnitIsNotEvenlyDistributedAccrossMachinesException(ProcessingUnit pu) {
        super(pu, "Instances are not evenly distributed accress machines.");
    }


}
