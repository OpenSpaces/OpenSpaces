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
package org.openspaces.grid.gsm.sla.exceptions;

import java.util.Arrays;

public class SlaEnforcementInProgressException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String[] puNames;
    
    public SlaEnforcementInProgressException(String[] puNames, String message) {
        super(message);
        this.puNames = puNames;
    }

    public SlaEnforcementInProgressException(String[] puNames, String message, Throwable cause) {
        super(message, cause);
        this.puNames = puNames;
    }
    
    /**
     * Override the method to avoid expensive stack build and synchronization,
     * since no one uses it anyway.
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }

    public String[] getAffectedProcessingUnits() {
        return puNames;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(puNames);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SlaEnforcementInProgressException other = (SlaEnforcementInProgressException) obj;
        if (!Arrays.equals(puNames, other.puNames))
            return false;
        
        //custom code
        
        if (other.getCause() == null && this.getCause() != null) {
            return false;
        }
        
        if (other.getCause() != null && this.getCause() == null) {
            return false;
        }
        
        if (other.getCause() != null && this.getCause() != null && !other.getCause().getMessage().equals(this.getCause().getMessage())) {
            return false;
        }
        return true;
    }
}
