package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

public class ProcessingUnitIsNotEvenlyDistributedAcrossContainersException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(ProcessingUnit pu, GridServiceContainer[] containers) {
        super(pu, "Instances are not evenly distributed accress containers: " + ContainersSlaUtils.gscsToString(containers));
    }

}
