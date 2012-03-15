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

import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FailedToStartNewMachineException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    
    public FailedToStartNewMachineException(String[] affectedProcessingUnits, Exception cause) {
        super("Machine provisioning failed to start a new machine. Cause:" + cause.getMessage(), cause);
        this.affectedProcessingUnits = affectedProcessingUnits;
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof FailedToStartNewMachineException) {
            FailedToStartNewMachineException otherEx = (FailedToStartNewMachineException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits,this.affectedProcessingUnits) && 
                   otherEx.getCause().getMessage().equals(getCause().getMessage());
        }
        return same;  
    }
}
