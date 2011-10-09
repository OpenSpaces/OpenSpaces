package org.openspaces.grid.gsm.sla.exceptions;

public class SlaEnforcementEndpointDestroyedException extends SlaEnforcementException {

    private static final long serialVersionUID = 1L;

    public SlaEnforcementEndpointDestroyedException() {
        super("SLA enforcement endpoint has already been destroyed");
    }

}
