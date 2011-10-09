package org.openspaces.grid.gsm.sla.exceptions;

public class SlaEnforcementInProgressException extends SlaEnforcementException {

    private static final long serialVersionUID = 1L;

    public SlaEnforcementInProgressException(String message) {
        super(message);
    }

    public SlaEnforcementInProgressException(String message, Exception reason) {
        super(message, reason);
    }

}
