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
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * An exception raised when autoscaling can no longer increase capacity since it reached the limit maximum.
 * @author itaif
 * @since 9.0.0
 */
public class ReachedMinimumCapacityAutoScalingException
    extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementFailure{

    private static final long serialVersionUID = 1L;
    
    private final CapacityRequirements existingCapacity;
    private final CapacityRequirements newCapacity;
    private final CapacityRequirements minCapacity;
    
    private final String puName;

    public ReachedMinimumCapacityAutoScalingException(
            ProcessingUnit pu,
            CapacityRequirements existingCapacity, 
            CapacityRequirements newCapacity, 
            CapacityRequirements minCapacity) {
    
        super("Cannot decrease capacity from " + existingCapacity + " to " + newCapacity + " since it breaches minimum capacity " + minCapacity);
        this.existingCapacity = existingCapacity;
        this.newCapacity = newCapacity;
        this.minCapacity = minCapacity;
        this.puName = pu.getName();
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return new String[] { puName};
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((existingCapacity == null) ? 0 : existingCapacity.hashCode());
        result = prime * result + ((minCapacity == null) ? 0 : minCapacity.hashCode());
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
        ReachedMinimumCapacityAutoScalingException other = (ReachedMinimumCapacityAutoScalingException) obj;
        if (existingCapacity == null) {
            if (other.existingCapacity != null)
                return false;
        } else if (!existingCapacity.equals(other.existingCapacity))
            return false;
        if (minCapacity == null) {
            if (other.minCapacity != null)
                return false;
        } else if (!minCapacity.equals(other.minCapacity))
            return false;
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