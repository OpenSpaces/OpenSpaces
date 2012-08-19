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

import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FailedToStartNewMachineException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    
    public FailedToStartNewMachineException(String[] affectedProcessingUnits, Exception cause) {
        super(affectedProcessingUnits, "Machine provisioning failed to start a new machine. Cause:" + cause.getMessage(), cause);
    }
    
    /**
     * Override the method to show stack trace for cloud specific exceptions.
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return ((Throwable)this).fillInStackTrace();
    }
}
