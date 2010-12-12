package org.openspaces.grid.gsm.sla;

public class ServiceLevelAgreementEnforcementEndpointDestroyedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServiceLevelAgreementEnforcementEndpointDestroyedException() {
        super("SLA enforcement endpoint has already been destroyed");
    }

}
