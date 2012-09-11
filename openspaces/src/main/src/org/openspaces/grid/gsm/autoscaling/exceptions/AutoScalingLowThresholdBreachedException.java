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
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementDecision;

/**
 * @author itaif
 * @since 9.0.1
 */
public class AutoScalingLowThresholdBreachedException extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementDecision {

    private static final long serialVersionUID = 1L;
    private final CapacityRequirements newCapacity;
    private final String puName;
    
    public AutoScalingLowThresholdBreachedException(
            ProcessingUnit pu,
            CapacityRequirements existingCapacity,
            CapacityRequirements newCapacity) {
        super(message(pu,existingCapacity,newCapacity));
        this.newCapacity = newCapacity;
        this.puName = pu.getName();
    }

    private static String message(ProcessingUnit pu, CapacityRequirements existingCapacity, CapacityRequirements newCapacity) {
        //TODO: Add rule, threshold, value that breached threshold, how value was calculated.
        return "Decreasing capacity of " + pu.getName() + " from " + existingCapacity + " to " + newCapacity;
    }

    public CapacityRequirements getNewCapacity() {
        return newCapacity;
    }

    @Override
    public String[] getAffectedProcessingUnits() {
        return new String[] { puName };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((newCapacity == null) ? 0 : newCapacity.hashCode());
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
        AutoScalingLowThresholdBreachedException other = (AutoScalingLowThresholdBreachedException) obj;
        if (newCapacity == null) {
            if (other.newCapacity != null)
                return false;
        } else if (!newCapacity.equals(other.newCapacity))
            return false;
        if (puName == null) {
            if (other.puName != null)
                return false;
        } else if (!puName.equals(other.puName))
            return false;
        return true;
    }

}
