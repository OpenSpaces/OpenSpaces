package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

public class MaximumNumberOfConcurrentRelocationsReachedException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public MaximumNumberOfConcurrentRelocationsReachedException(ProcessingUnit pu, GridServiceContainer container) {
        super(pu,maxConcurrentMessage(container));
    }

    private static String maxConcurrentMessage(GridServiceContainer container) {
        return "Maximum number of concurrent relocations reached for container " + ContainersSlaUtils.gscToString(container);
    }

}
