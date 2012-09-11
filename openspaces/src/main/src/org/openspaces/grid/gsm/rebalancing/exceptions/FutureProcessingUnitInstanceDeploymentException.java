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
package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticProcessingUnitInstanceProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FutureProcessingUnitInstanceDeploymentException extends RebalancingSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    
    public FutureProcessingUnitInstanceDeploymentException(ProcessingUnit pu, Throwable cause) {
        super(pu, message(pu, cause), cause);
    }

    private static String message(ProcessingUnit pu, Throwable cause) {
        String causeMessage = (cause != null && cause.getMessage()!=null) ? cause.getMessage() : "";
        return pu.getName() + " instance deployment failed." + causeMessage;
    }
    
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticProcessingUnitInstanceProvisioningFailureEvent event = new DefaultElasticProcessingUnitInstanceProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitNames(getAffectedProcessingUnits());
        return event;
    }
}
