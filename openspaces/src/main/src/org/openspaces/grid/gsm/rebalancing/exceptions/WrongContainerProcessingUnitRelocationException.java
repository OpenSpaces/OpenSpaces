package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

public class WrongContainerProcessingUnitRelocationException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1239832832671562407L;

    public WrongContainerProcessingUnitRelocationException(
            ProcessingUnitInstance instance, 
            GridServiceContainer expectedContainer) {
        
        super(instance.getProcessingUnit(),
                "Relocation of processing unit instance " + instance.getProcessingUnitInstanceName()+ " to container " +
                ContainersSlaUtils.gscToString(expectedContainer) + " "+
                "failed since the instance was eventually deployed on a different container " + 
                ContainersSlaUtils.gscToString(instance.getGridServiceContainer()));
    }

}
