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

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.rebalancing.RebalancingUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public class RebalancingSlaEnforcementInProgressException extends SlaEnforcementInProgressException {
 private static final long serialVersionUID = 1L;
    
    public RebalancingSlaEnforcementInProgressException(ProcessingUnit pu) {
        super(inProgressMessage(pu));
    }
    
    public RebalancingSlaEnforcementInProgressException(ProcessingUnit pu, Throwable cause) {
        super(inProgressMessage(pu,cause), cause);
    }

    public RebalancingSlaEnforcementInProgressException(ProcessingUnit pu, String message) {
        super(inProgressMessage(pu)+": " + message + 
                " Instances " + RebalancingUtils.processingUnitDeploymentToString(pu) + 
                " Status = " + pu.getStatus());
    }
    
    private static String inProgressMessage(ProcessingUnit pu) {
        return inProgressMessage(pu, null);
    }
    
    private static String inProgressMessage(ProcessingUnit pu, Throwable cause) {
        String causeMessage = "";
        if (cause != null) {
            causeMessage = cause.getMessage();
        }
        return pu.getName() + " rebalancing SLA enforcement is in progress." + causeMessage;
    }
}
