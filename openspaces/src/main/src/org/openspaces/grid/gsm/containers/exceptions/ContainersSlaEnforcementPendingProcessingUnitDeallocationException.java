package org.openspaces.grid.gsm.containers.exceptions;

import java.util.Collection;

import org.openspaces.admin.gsc.GridServiceContainer;

public class ContainersSlaEnforcementPendingProcessingUnitDeallocationException extends ContainersSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public ContainersSlaEnforcementPendingProcessingUnitDeallocationException(Collection<GridServiceContainer> containers) {
        super("Cannot shutdown the following containers untill all processing units have been undeployed: " + containers);
    }
    

}
