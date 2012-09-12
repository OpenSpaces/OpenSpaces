/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.sla;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

public interface ServiceLevelAgreementEnforcement<ENDPOINT extends ServiceLevelAgreementEnforcementEndpoint> {

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
