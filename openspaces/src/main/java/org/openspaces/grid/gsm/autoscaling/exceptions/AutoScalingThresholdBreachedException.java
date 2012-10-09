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
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
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
    private final CapacityRequirements newPlan;
    private final CapacityRequirements actual;
    private final ProcessingUnit pu;
    private final long containerCapacityInMB;
    private CapacityRequirements oldPlan;
    private AutomaticCapacityScaleRuleConfig rule;
    private boolean highThresholdBreached;
    private String metricValue;
    
    /**
     * 
     * @param message - the exception specific message
     * @param pu - the processing unit being monitored
     * @param actual - the actual capacity currently deployed
     * @param newPlan - the new planned capacity to be deployed
     * @param containerCapacityInMB
     * @param rule - the rule whos threshold was breached
     * @param highThresholdBreached - true means high threshold breached, false means low threshold breached
     * @param metricValue - the metric value that breached the threshold as a string.
     */
    public AutoScalingThresholdBreachedException(
            String message,
            ProcessingUnit pu,
            CapacityRequirements actual,
            CapacityRequirements newPlan,
            long containerCapacityInMB, 
            AutomaticCapacityScaleRuleConfig rule, 
            boolean highThresholdBreached,  
            String metricValue) {
        
        super(pu, message);
        this.actual = actual;
        this.newPlan = newPlan;
        this.pu = pu;
        this.containerCapacityInMB = containerCapacityInMB;
        this.rule = rule;
        this.highThresholdBreached = highThresholdBreached;
        this.metricValue = metricValue;
    }

    public CapacityRequirements getNewCapacity() {
        return newPlan;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((actual == null) ? 0 : actual.hashCode());
        result = prime * result + (int) (containerCapacityInMB ^ (containerCapacityInMB >>> 32));
        result = prime * result + ((newPlan == null) ? 0 : newPlan.hashCode());
        result = prime * result + ((oldPlan == null) ? 0 : oldPlan.hashCode());
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
        if (actual == null) {
            if (other.actual != null)
                return false;
        } else if (!actual.equals(other.actual))
            return false;
        if (containerCapacityInMB != other.containerCapacityInMB)
            return false;
        if (newPlan == null) {
            if (other.newPlan != null)
                return false;
        } else if (!newPlan.equals(other.newPlan))
            return false;
        if (oldPlan == null) {
            if (other.oldPlan != null)
                return false;
        } else if (!oldPlan.equals(other.oldPlan))
            return false;
        return true;
    }

    public void setOldPlan(CapacityRequirements oldPlan) {
        this.oldPlan = oldPlan;
    }
    
    @Override
    public InternalElasticProcessingUnitDecisionEvent toEvent() {
        
        CapacityRequirementsConfig actualConfig = new CapacityRequirementsConfig(actual);
        CapacityRequirementsConfig newPlanConfig = new CapacityRequirementsConfig(newPlan);
        CapacityRequirementsConfig oldPlanConfig = new CapacityRequirementsConfig(oldPlan);
        
        if (pu.getType().equals(ProcessingUnitType.STATEFUL)) {
            return new ElasticStatefulProcessingUnitPlannedCapacityChangedEvent(actualConfig, newPlanConfig);
        }

        int actualNumberOfInstances = (int) Math.ceil((actualConfig.getMemoryCapacityInMB() * 1.0 / containerCapacityInMB));
        int newPlannedNumberOfInstances = (int) Math.ceil((newPlanConfig.getMemoryCapacityInMB() * 1.0 / containerCapacityInMB));
        int oldPlanmedNumberOfInstances = (int) Math.ceil((oldPlanConfig.getMemoryCapacityInMB() * 1.0 / containerCapacityInMB));
        return new ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent(
                actualNumberOfInstances, oldPlanmedNumberOfInstances, newPlannedNumberOfInstances, rule, highThresholdBreached, metricValue);
    }
}
