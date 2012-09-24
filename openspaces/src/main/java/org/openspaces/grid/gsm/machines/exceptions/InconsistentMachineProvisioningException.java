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
package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;
import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;

public class InconsistentMachineProvisioningException extends GridServiceAgentSlaEnforcementInProgressException{
    
    private static final long serialVersionUID = 1L;

    public InconsistentMachineProvisioningException(ProcessingUnit pu, Collection<GridServiceAgent> undiscoveredAgents) {
        super(pu, "Machines " + MachinesSlaUtils.machinesToString(undiscoveredAgents)+ " have not been discoved yet by the machine provisioning.");
    }
    
    public InconsistentMachineProvisioningException(ProcessingUnit pu, GridServiceAgent undiscoveredAgents) {
        this(pu, Arrays.asList(new GridServiceAgent[] {undiscoveredAgents}));
    }
    
}
