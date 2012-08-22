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
import org.openspaces.admin.zone.config.ZonesConfig;
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
    private CapacityRequirements maxCapacity;
    private CapacityRequirements minCapacity;
    private ZonesConfig zonesConfig;
    
    /**
     * @return the zonesConfig
     */
    public ZonesConfig getZonesConfig() {
        return zonesConfig;
    }

    /**
     * @param zonesConfig the zonesConfig to set
     */
    public void setZonesConfig(ZonesConfig zonesConfig) {
        this.zonesConfig = zonesConfig;
    }

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

    public CapacityRequirements getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(CapacityRequirements maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public CapacityRequirements getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(CapacityRequirements minCapacity) {
        this.minCapacity = minCapacity;
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
        
        if (maxCapacity == null) {
            throw new IllegalArgumentException("maxCapacity cannot be null");
        }
        
        if (maxCapacity.equalsZero()) {
            throw new IllegalArgumentException("maxCapacity cannot be zero");
        }
        
        if (minCapacity == null) {
            throw new IllegalArgumentException("maxCapacity cannot be null");
        }
        
        if (minCapacity.equalsZero()) {
            throw new IllegalArgumentException("minCapacity cannot be zero");
        }
        
        if (zonesConfig == null) {
            throw new IllegalArgumentException("zonesConfig cannot be null");
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AutoScalingSlaPolicy [capacityRequirements=" + capacityRequirements + ", rules="
                + Arrays.toString(rules) + ", maxCapacity=" + maxCapacity + ", minCapacity=" + minCapacity
                + ", zonesConfig=" + zonesConfig + "]";
    }



}
