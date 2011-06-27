package org.openspaces.grid.gsm.sla;

import org.openspaces.admin.pu.ProcessingUnit;

public interface ServiceLevelAgreementEnforcement
    <POLICY extends ServiceLevelAgreementPolicy,
     ENDPOINT extends ServiceLevelAgreementEnforcementEndpoint<POLICY>> {

    /**
     * Creates a new endpoint for the specified pu, if one does not already exist.
     * If an endpoint already exists an exception is raised. 
     * @param pu target processing unit for the endpoint creation
     * @return the new endpoint
     * @throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException
     */
    ENDPOINT createEndpoint(ProcessingUnit pu) throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
    
    /**
     * Destroys the endpoint for the specified pu. 
     * @param pu target processing unit for the endpoint destruction 
     */
    void destroyEndpoint(ProcessingUnit pu);
}
