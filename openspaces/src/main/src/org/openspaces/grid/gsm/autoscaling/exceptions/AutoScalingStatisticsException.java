/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.grid.gsm.autoscaling.exceptions;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * @author itaif
 * @since 9.0.0
 */
public class AutoScalingStatisticsException extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementFailure{

    private final String puName;

    public AutoScalingStatisticsException(ProcessingUnit pu, String message, Exception reason) {
        super(message, reason);
        this.puName = pu.getName();
    }

    private static final long serialVersionUID = 1L;

    @Override
    public String[] getAffectedProcessingUnits() {
        return new String[] { puName};
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((puName == null) ? 0 : puName.hashCode());
        return result;
    }

    /**
     * Compares also cause.getMessage()
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AutoScalingStatisticsException other = (AutoScalingStatisticsException) obj;
        if (puName == null) {
            if (other.puName != null)
                return false;
        } else if (!puName.equals(other.puName))
            return false;
        
        if (getCause() != null && getCause().getMessage() != null && 
            !getCause().getMessage().equals(other.getCause().getMessage())) {
            return false;
        }
        
        return true;
    }

    
}
