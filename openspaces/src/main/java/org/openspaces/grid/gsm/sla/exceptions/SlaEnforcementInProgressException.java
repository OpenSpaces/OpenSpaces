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

import org.openspaces.admin.pu.ProcessingUnit;

public class SlaEnforcementInProgressException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String puName;
    
    public SlaEnforcementInProgressException(ProcessingUnit pu, String message) {
        this(pu.getName(), message);
    }
    
    public SlaEnforcementInProgressException(ProcessingUnit pu, String message, Throwable cause) {
        this(pu.getName(), message, cause);
    }
    
    public SlaEnforcementInProgressException(String puName, String message) {
        super(message);
        this.puName = puName;
    }

    public SlaEnforcementInProgressException(String puName, String message, Throwable cause) {
        super(message, cause);
        this.puName = puName;
    }
    
    /**
     * Override the method to avoid expensive stack build and synchronization,
     * since no one uses it anyway.
     */
    @Override
    public Throwable fillInStackTrace()
    {
        if (this instanceof SlaEnforcementLogStackTrace) {
            return super.fillInStackTrace();
        }
        return this;
    }

    public String getProcessingUnitName() {
        return puName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((puName == null) ? 0 : puName.hashCode());
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
        if (puName == null) {
            if (other.puName != null)
                return false;
        } else if (!puName.equals(other.puName))
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
