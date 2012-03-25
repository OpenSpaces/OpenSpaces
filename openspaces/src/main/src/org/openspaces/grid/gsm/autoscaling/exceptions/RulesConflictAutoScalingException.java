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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * An exception that is raised when there is more than one scaling rule,
 * and one or more scaling rule requires scale out, while the other requires scale down.
 * @author itaif
 * @since 9.0.0
 */
public class RulesConflictAutoScalingException  extends AutoScalingSlaEnforcementInProgressException 
    implements SlaEnforcementFailure{

    private static final long serialVersionUID = 1L;
    private final String puName;
    private final Set<AutomaticCapacityScaleRuleConfig> valuesBelowLowThresholdPerRule;
    private final Set<AutomaticCapacityScaleRuleConfig> valuesAboveHighThresholdPerRule;
    
    public RulesConflictAutoScalingException(InternalProcessingUnit pu,
            Map<AutomaticCapacityScaleRuleConfig, Object> valuesBelowLowThresholdPerRule,
            Map<AutomaticCapacityScaleRuleConfig, Object> valuesAboveHighThresholdPerRule) {
        
        super(message(valuesBelowLowThresholdPerRule, valuesAboveHighThresholdPerRule));
        this.valuesBelowLowThresholdPerRule = valuesBelowLowThresholdPerRule.keySet();
        this.valuesAboveHighThresholdPerRule = valuesAboveHighThresholdPerRule.keySet();
        puName = pu.getName();
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return new String[] { puName};
    }
    
    private static String message(
            Map<AutomaticCapacityScaleRuleConfig, Object> valuesBelowLowThresholdPerRule,
            Map<AutomaticCapacityScaleRuleConfig, Object> valuesAboveHighThresholdPerRule) {
        
        List<String> messages = new ArrayList<String>(valuesAboveHighThresholdPerRule.size() + valuesBelowLowThresholdPerRule.size());
        for (Entry<AutomaticCapacityScaleRuleConfig, Object> pair : valuesAboveHighThresholdPerRule.entrySet()) {
            AutomaticCapacityScaleRuleConfig rule = pair.getKey();
            Object value = pair.getValue();
            messages.add(rule.getStatistics().getMetric() + " value (" + value +") is above high threshold " + rule.getHighThreshold());
        }
        for (Entry<AutomaticCapacityScaleRuleConfig, Object> pair : valuesBelowLowThresholdPerRule.entrySet()) {
            AutomaticCapacityScaleRuleConfig rule = pair.getKey();
            Object value = pair.getValue();
            messages.add(rule.getStatistics().getMetric() + " value (" + value +") is below low threshold " + rule.getHighThreshold());
        }
        String message = Arrays.toString(messages.toArray(new String[messages.size()]));
        return "Rule conflict, taking no automatic action. " + message;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((puName == null) ? 0 : puName.hashCode());
        result = prime * result
                + ((valuesAboveHighThresholdPerRule == null) ? 0 : valuesAboveHighThresholdPerRule.hashCode());
        result = prime * result
                + ((valuesBelowLowThresholdPerRule == null) ? 0 : valuesBelowLowThresholdPerRule.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RulesConflictAutoScalingException other = (RulesConflictAutoScalingException) obj;
        if (puName == null) {
            if (other.puName != null)
                return false;
        } else if (!puName.equals(other.puName))
            return false;
        if (valuesAboveHighThresholdPerRule == null) {
            if (other.valuesAboveHighThresholdPerRule != null)
                return false;
        } else if (!valuesAboveHighThresholdPerRule.equals(other.valuesAboveHighThresholdPerRule))
            return false;
        if (valuesBelowLowThresholdPerRule == null) {
            if (other.valuesBelowLowThresholdPerRule != null)
                return false;
        } else if (!valuesBelowLowThresholdPerRule.equals(other.valuesBelowLowThresholdPerRule))
            return false;
        return true;
    }
}
