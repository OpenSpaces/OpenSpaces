package org.openspaces.grid.gsm.containers.exceptions;

import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public class ContainersSlaEnforcementInProgressException extends SlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public ContainersSlaEnforcementInProgressException(String message) {
        super(message);
    }
    
}
