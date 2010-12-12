package org.openspaces.grid.gsm.sla;

public interface ServiceLevelAgreementEnforcement
    <POLICY extends ServiceLevelAgreementPolicy,
     ID,
     ENDPOINT extends ServiceLevelAgreementEnforcementEndpoint<ID,POLICY>> {

    ENDPOINT createEndpoint(ID id) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
    
    void destroyEndpoint(ID id);
}
