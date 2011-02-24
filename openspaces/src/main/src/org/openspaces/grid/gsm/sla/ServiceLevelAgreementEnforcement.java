package org.openspaces.grid.gsm.sla;

import org.openspaces.admin.pu.ProcessingUnit;

public interface ServiceLevelAgreementEnforcement
    <POLICY extends ServiceLevelAgreementPolicy,
     ENDPOINT extends ServiceLevelAgreementEnforcementEndpoint<POLICY>> {

    /**
     * Creates a new endpoint for the specified id, if one does not already exist.
     * If an endpoint already exists an exception is raised. 
     * @param id
     * @return the new endpoint
     * @throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException
     */
    ENDPOINT createEndpoint(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
    
    /**
     * Destroys the endpoint with the specified id. 
     * @param id
     */
    void destroyEndpoint(ProcessingUnit pu);
}
