package org.openspaces.grid.gsm.sla;

public interface ServiceLevelAgreementEnforcement
    <POLICY extends ServiceLevelAgreementPolicy,
     ID,
     ENDPOINT extends ServiceLevelAgreementEnforcementEndpoint<ID,POLICY>> {

    /**
     * Creates a new endpoint for the specified id, if one does not already exist.
     * If an endpoint already exists an exception is raised. 
     * @param id
     * @return the new endpoint
     * @throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException
     */
    ENDPOINT createEndpoint(ID id) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
    
    /**
     * Destroys the endpoint with the specified id. 
     * @param id
     */
    void destroyEndpoint(ID id);
}
