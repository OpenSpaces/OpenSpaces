package org.openspaces.grid.gsm.sla.exceptions;

public class SlaEnforcementInProgressException extends Exception {

    private static final long serialVersionUID = 1L;

    public SlaEnforcementInProgressException(String message) {
        super(message);
    }

    public SlaEnforcementInProgressException(String message, Exception reason) {
        super(message, reason);
    }
    
    /**
     * Override the method to avoid expensive stack build and synchronization,
     * since no one uses it anyway.
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return null;
    }

}
