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

import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitDecisionEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.events.ElasticStatefulProcessingUnitPlannedCapacityChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementDecision;

/**
 * Base class for high/low threshold breached exceptions
 * @author Itai Frenkel
 * @since 9.1.0
 */
public abstract class AutoScalingThresholdBreachedException extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementDecision {

    private static final long serialVersionUID = 1L;
    private final CapacityRequirements after;
    private final CapacityRequirements before;
    private final ProcessingUnit pu;
    private final long containerCapacityInMB;
    
    public AutoScalingThresholdBreachedException(
            String message,
            ProcessingUnit pu,
            CapacityRequirements before,
            CapacityRequirements after,
            long containerCapacityInMB) {
        super(pu, message);
        this.before = before;
        this.after = after;
        this.pu = pu;
        this.containerCapacityInMB = containerCapacityInMB;
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
        result = prime * result + (int) (containerCapacityInMB ^ (containerCapacityInMB >>> 32));
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
        AutoScalingThresholdBreachedException other = (AutoScalingThresholdBreachedException) obj;
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
        if (containerCapacityInMB != other.containerCapacityInMB)
            return false;
        return true;
    }

    @Override
    public InternalElasticProcessingUnitDecisionEvent toEvent() {
        
        CapacityRequirementsConfig beforeConfig = new CapacityRequirementsConfig(before);
        CapacityRequirementsConfig afterConfig = new CapacityRequirementsConfig(after);
        
        if (pu.getType().equals(ProcessingUnitType.STATEFUL)) {
            return new ElasticStatefulProcessingUnitPlannedCapacityChangedEvent(beforeConfig, afterConfig);
        }

        int beforeNumberOfInstances = (int) Math.ceil((beforeConfig.getMemoryCapacityInMB() * 1.0 / containerCapacityInMB));
        int afterNumberOfInstances = (int) Math.ceil((afterConfig.getMemoryCapacityInMB() * 1.0 / containerCapacityInMB));
        return new ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent(beforeNumberOfInstances ,afterNumberOfInstances);
    }
}
