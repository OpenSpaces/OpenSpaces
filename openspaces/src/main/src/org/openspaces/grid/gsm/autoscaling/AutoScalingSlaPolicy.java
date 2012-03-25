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
package org.openspaces.grid.gsm.autoscaling;

import java.util.Arrays;

import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

/**
 * Input argument for {@link AutoScalingSlaEnforcementEndpoint#enforceSla(AutoScalingSlaPolicy)}
 * @author itaif
 * @since 9.0.0
 */
/**
 * @author itaif
 *
 */
public class AutoScalingSlaPolicy extends ServiceLevelAgreementPolicy {

    private CapacityRequirements capacityRequirements;
    private AutomaticCapacityScaleRuleConfig[] rules;
    private CapacityRequirements highThresholdBreachedIncrease;
    private CapacityRequirements maxCapacity;
    
    public CapacityRequirements getCapacityRequirements() {
        return capacityRequirements;
    }
    
    public void setCapacityRequirements(CapacityRequirements capacityRequirements) {
        this.capacityRequirements=capacityRequirements;
    }

    public AutomaticCapacityScaleRuleConfig[] getRules() {
        return rules;
    }
    
    public void setRules(AutomaticCapacityScaleRuleConfig[] rules) {
        this.rules = rules;
    }
    
    public void setHighThresholdBreachedIncrease(CapacityRequirements highThresholdBreachedIncrease) {
        this.highThresholdBreachedIncrease = highThresholdBreachedIncrease;
    }

    public CapacityRequirements getHighThresholdBreachedIncrease() {
        return highThresholdBreachedIncrease;
    }

    public CapacityRequirements getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(CapacityRequirements maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capacityRequirements == null) ? 0 : capacityRequirements.hashCode());
        result = prime * result
                + ((highThresholdBreachedIncrease == null) ? 0 : highThresholdBreachedIncrease.hashCode());
        result = prime * result + ((maxCapacity == null) ? 0 : maxCapacity.hashCode());
        result = prime * result + Arrays.hashCode(rules);
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
        AutoScalingSlaPolicy other = (AutoScalingSlaPolicy) obj;
        if (capacityRequirements == null) {
            if (other.capacityRequirements != null)
                return false;
        } else if (!capacityRequirements.equals(other.capacityRequirements))
            return false;
        if (highThresholdBreachedIncrease == null) {
            if (other.highThresholdBreachedIncrease != null)
                return false;
        } else if (!highThresholdBreachedIncrease.equals(other.highThresholdBreachedIncrease))
            return false;
        if (maxCapacity == null) {
            if (other.maxCapacity != null)
                return false;
        } else if (!maxCapacity.equals(other.maxCapacity))
            return false;
        if (!Arrays.equals(rules, other.rules))
            return false;
        return true;
    }

    @Override
    public void validate() throws IllegalArgumentException {

        if (capacityRequirements == null) {
            throw new IllegalArgumentException("capacityRequirements cannot be null");
        }
        
        if (rules == null) {
            throw new IllegalArgumentException("rules cannot be null");
        }
        
        if (rules.length == 0) {
            throw new IllegalArgumentException("rules cannot be empty");
        }
        
        if (highThresholdBreachedIncrease == null) {
            throw new IllegalArgumentException("highThresholdBreachedIncrease cannot be null");
        }
        
        if (highThresholdBreachedIncrease.equalsZero()) {
            throw new IllegalArgumentException("highThresholdBreachedIncrease cannot be zero");
        }
        
        if (maxCapacity == null) {
            throw new IllegalArgumentException("maxCapacity cannot be null");
        }
        
        if (maxCapacity.equalsZero()) {
            throw new IllegalArgumentException("maxCapacity cannot be zero");
        }
    }

    @Override
    public String toString() {
        return "AutoScalingSlaPolicy [capacityRequirements=" + capacityRequirements + ", rules="
                + Arrays.toString(rules) + ", highThresholdBreachedIncrease=" + highThresholdBreachedIncrease
                + ", maxCapacity=" + maxCapacity + "]";
    }
    
    
}
