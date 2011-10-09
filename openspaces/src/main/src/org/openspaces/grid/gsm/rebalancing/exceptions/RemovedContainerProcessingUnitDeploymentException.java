package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

public class RemovedContainerProcessingUnitDeploymentException extends RebalancingSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public RemovedContainerProcessingUnitDeploymentException(
            ProcessingUnitInstance instance, 
            GridServiceContainer expectedContainer) {
        
        super(instance.getProcessingUnit(),
                "Relocation of processing unit instance " + instance.getProcessingUnitInstanceName() +
                " on container " + 
                ContainersSlaUtils.gscToString(expectedContainer) + " "+
                "failed since container no longer exists.");
    }
    
    public RemovedContainerProcessingUnitDeploymentException(
            ProcessingUnit pu, 
            GridServiceContainer expectedContainer) {
        
        super(pu,
                "Deployment of processing unit " + pu.getName() +
                " on container " + 
                ContainersSlaUtils.gscToString(expectedContainer) + " "+
                "failed since container no longer exists.");
    }
}
