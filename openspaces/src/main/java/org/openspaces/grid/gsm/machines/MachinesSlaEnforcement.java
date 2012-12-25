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
package org.openspaces.grid.gsm.machines;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.exceptions.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

/**
 * Enforces the MachinesSlaPolicy of all processing units by starting an enforcement endpoint for each PU.
 * The state is shared by all endpoints to detect conflicting operations.  
 * @author itaif
 *
 */
public class MachinesSlaEnforcement implements
        ServiceLevelAgreementEnforcement<MachinesSlaEnforcementEndpoint> {

    private final MachinesSlaEnforcementState state;
    private final Map<ProcessingUnit, MachinesSlaEnforcementEndpoint> endpoints;

    public MachinesSlaEnforcement() {
        this.endpoints = new HashMap<ProcessingUnit, MachinesSlaEnforcementEndpoint>();
        this.state = new MachinesSlaEnforcementState();
    }

    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        
        if (!isEndpointDestroyed(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }
        
        MachinesSlaEnforcementEndpoint endpoint = new DefaultMachinesSlaEnforcementEndpoint(pu, state);
        endpoints.put(pu, endpoint);
        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {
        endpoints.remove(pu);
    }

    public void destroy() throws Exception {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }
    
    private boolean isEndpointDestroyed(ProcessingUnit pu) {
        return endpoints.get(pu) == null;
    }

    public void failedRecoveredStateOnEsmStart(ProcessingUnit pu) {
        this.state.failedRecoveredStateOnEsmStart(pu);
    }
}
