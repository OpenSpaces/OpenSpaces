package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public class GridServiceAgentSlaEnforcementInProgressException extends SlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    
    public GridServiceAgentSlaEnforcementInProgressException(String message) {
        super(message);
    }

}
