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
package org.openspaces.grid.gsm.containers;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.exceptions.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;

/**
 * Starts and shutdowns grid service container based on the requested {@link ContainersSlaPolicy}
 * 
 * Use {@link #createEndpoint(ProcessingUnit))} to enforce an SLA for a specific container zone.
 * 
 * @see ContainersSlaEnforcementEndpoint
 * @see ContainersSlaPolicy
 * @author itaif
 *
 */
public class ContainersSlaEnforcement implements
        ServiceLevelAgreementEnforcement<ContainersSlaEnforcementEndpoint> {

    private final ContainersSlaEnforcementState state;
    private final Map<ProcessingUnit, ContainersSlaEnforcementEndpoint> endpoints;

    public ContainersSlaEnforcement(Admin admin) {
        this.endpoints = new HashMap<ProcessingUnit, ContainersSlaEnforcementEndpoint>();
        this.state = new ContainersSlaEnforcementState();
    }

    /**
     * 
     * @return a service that continuously maintains the specified number of containers for the
     *         specified pu.
     */
    public ContainersSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {

        if (!isEndpointDestroyed(pu)) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for the pu already exists.");
        }

        ProcessingUnit otherPu1 = ContainersSlaUtils.findProcessingUnitWithSameName(endpoints.keySet(), pu);
        if (otherPu1 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same name already exists.");
        }

        ProcessingUnit otherPu2 = ContainersSlaUtils.findProcessingUnitWithSameZone(endpoints.keySet(), pu);
        if (otherPu2 != null) {
            throw new IllegalStateException("Cannot initialize a new ContainersSlaEnforcementEndpoint for pu "
                    + pu.getName() + " since an endpoint for a pu with the same (containers) zone already exists: "
                    + otherPu2.getName());
        }

        ContainersSlaEnforcementEndpoint endpoint = new DefaultContainersSlaEnforcementEndpoint(pu, state);
        endpoints.put(pu, endpoint);
        state.initProcessingUnit(pu);
        
        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {
        state.destroyProcessingUnit(pu);
        endpoints.remove(pu);
    }

    public void destroy() {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }

    private boolean isEndpointDestroyed(ProcessingUnit pu) {

        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        return !endpoints.containsKey(pu) || state.isProcessingUnitDestroyed(pu);
    }

    
    
}
