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
public class AutoScalingHighThresholdBreachedException extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementDecision {

    private static final long serialVersionUID = 1L;
    private final CapacityRequirements after;
    private final CapacityRequirements before;
    
    public AutoScalingHighThresholdBreachedException(
            ProcessingUnit pu,
            CapacityRequirements before,
            CapacityRequirements after) {
        super(pu, message(pu, before, after));
        this.before = before;
        this.after = after;
    }

    private static String message(ProcessingUnit pu, CapacityRequirements before, CapacityRequirements after) {
        //TODO: Add rule, threshold, value that breached threshold, how value was calculated.
        return "Increasing capacity of " + pu.getName() + " from " + before + " to " + after;
    }

    public CapacityRequirements getNewCapacity() {
        return after;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((after == null) ? 0 : after.hashCode());
        result = prime * result + ((before == null) ? 0 : before.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AutoScalingHighThresholdBreachedException other = (AutoScalingHighThresholdBreachedException) obj;
        if (after == null) {
            if (other.after != null)
                return false;
        } else if (!after.equals(other.after))
            return false;
        if (before == null) {
            if (other.before != null)
                return false;
        } else if (!before.equals(other.before))
            return false;
        return true;
    }
}
