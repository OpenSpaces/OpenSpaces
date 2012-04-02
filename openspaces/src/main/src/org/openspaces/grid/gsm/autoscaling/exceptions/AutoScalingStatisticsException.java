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
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

/**
 * @author itaif
 * @since 9.0.0
 */
public class AutoScalingStatisticsException extends AutoScalingSlaEnforcementInProgressException {

    private final String puName;

    protected AutoScalingStatisticsException(ProcessingUnit pu, String message) {
        super(message);
        this.puName = pu.getName();
    }
    
    protected AutoScalingStatisticsException(ProcessingUnit pu, String message, Throwable reason) {
        super(message, reason);
        this.puName = pu.getName();
    }
    
    public AutoScalingStatisticsException(ProcessingUnit pu, ProcessingUnitStatisticsId statisticsId) {
        this(pu,message(statisticsId,pu));
    }

    /**
     * Generates the default message
     * @param pu 
     */
    private static String message(ProcessingUnitStatisticsId statisticsId, ProcessingUnit pu) {
        return "No " + pu.getName() + " statistics for " + statisticsId;
    }

    private static final long serialVersionUID = 1L;

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
        
        if (getMessage() != null && !getMessage().endsWith(other.getMessage())) {
            return false;
        }
        
        if (getCause() != null && getCause().getMessage() != null && 
            !getCause().getMessage().equals(other.getCause().getMessage())) {
            return false;
        }
        
        return true;
    }

    
}
