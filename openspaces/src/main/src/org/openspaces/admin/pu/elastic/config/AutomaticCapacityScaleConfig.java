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
package org.openspaces.admin.pu.elastic.config;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.AutomaticCapacityScaleStrategyBean;

/**
 * Defines an automatic scaling strategy that increases and decreases capacity.
 * The trigger is when a monitored values crosses a threshold 
 *  
 * @see AutomaticCapacityScaleConfigurer
 * @since 9.0
 * @author itaif
 */
public class AutomaticCapacityScaleConfig 
    implements ScaleStrategyConfig , Externalizable {
    
    private static final long serialVersionUID = 1L;
    
    private static final String STATISTICS_POLLING_INTERVAL_SECONDS_KEY = "statistics-polling-interval-seconds";
    private static final long STATISTICS_POLLING_INTERVAL_SECONDS_DEFAULT = 60;

    private static final String COOLDOWN_AFTER_INSTANCE_ADDED_SECONDS_KEY = "cooldown-after-instance-added-seconds";
    private static final long COOLDOWN_AFTER_INSTANCE_ADDED_SECONDS_DEFAULT = 60;

    private static final String COOLDOWN_AFTER_INSTANCE_REMOVED_SECONDS_KEY = "cooldown-after-instance-removed-seconds";
    private static final long COOLDOWN_AFTER_INSTANCE_REMOVED_SECONDS_DEFAULT = 60;
    
    private static final String MIN_CAPACITY_KEY_PREFIX = "min-capacity.";
    private static final String INITIAL_CAPACITY_KEY_PREFIX = "initial-capacity";
    private static final String MAX_CAPACITY_KEY_PREFIX = "max-capacity.";
    private static final HashMap<String, String> EMPTY_CAPACITY = new HashMap<String, String>();

    private static final String RULE_KEY = "rule";

    
    
    private StringProperties properties;
    
    /**
     * Default constructor
     */
    public AutomaticCapacityScaleConfig() {
        this.properties = new StringProperties();
    }
       
    public AutomaticCapacityScaleConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    @Override
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }
    
    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    @Override
    public String getBeanClassName() {
        return AutomaticCapacityScaleStrategyBean.class.getName();
    }
    
    @Override    
    public int getMaxConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaxConcurrentRelocationsPerMachine(properties);
    }
    
    @Override    
    public void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaxConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }
    
    @Override
    public void setPollingIntervalSeconds(int pollingTimeIntervalSeconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties, pollingTimeIntervalSeconds);
    }

    @Override
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }
    
    @Override
    public boolean isAtMostOneContainerPerMachine() {
        return ScaleStrategyConfigUtils.isSingleContainerPerMachine(properties);
    }

    @Override
    public void setAtMostOneContainerPerMachine(boolean atMostOneContainerPerMachine) {
        ScaleStrategyConfigUtils.setAtMostOneContainerPerMachine(properties, atMostOneContainerPerMachine);
    }
    
    
    /**
     * Sets the polling rate in which statistics are gathered.
     */
    public void setStatisticsPollingIntervalSeconds(int statisticsPollingIntervalSeconds) {
        properties.putLong(STATISTICS_POLLING_INTERVAL_SECONDS_KEY, statisticsPollingIntervalSeconds);
    }

    public long getStatisticsPollingIntervalSeconds() {
        return properties.getLong(STATISTICS_POLLING_INTERVAL_SECONDS_KEY, STATISTICS_POLLING_INTERVAL_SECONDS_DEFAULT);
    }
    
    /**
     * Sets the number of seconds after a {@link ProcessingUnitInstance} is added and the 
     * {@link ProcessingUnit#getStatus()} is {@link DeploymentStatus#INTACT} that all
     * scaling rules are disabled.
     * 
     * Must be equal or bigger than {@link #setStatisticsPollingIntervalSeconds(long)}
     */
    public void setCooldownAfterInstanceAddedSeconds(long cooldownAfterInstanceAddedSeconds) {
        properties.putLong(COOLDOWN_AFTER_INSTANCE_ADDED_SECONDS_KEY, cooldownAfterInstanceAddedSeconds);
    }
    
    public long getCooldownAfterInstanceAddedSeconds() {
        return properties.getLong(COOLDOWN_AFTER_INSTANCE_ADDED_SECONDS_KEY, COOLDOWN_AFTER_INSTANCE_ADDED_SECONDS_DEFAULT);
    }

    /**
     * Sets the number of seconds after a {@link ProcessingUnitInstance} is removed and the 
     * {@link ProcessingUnit#getStatus()} is {@link DeploymentStatus#INTACT} that all
     * scaling rules are disabled.
     * 
     * Must be equal or bigger than {@link #setStatisticsPollingIntervalSeconds(long)}
     */
    public void setCooldownAfterInstanceRemovedSeconds(long cooldownAfterInstanceRemovedSeconds) {
        properties.putLong(COOLDOWN_AFTER_INSTANCE_REMOVED_SECONDS_KEY, cooldownAfterInstanceRemovedSeconds);
    }
    
    public long getCooldownAfterInstanceRemovedSeconds() {
        return properties.getLong(COOLDOWN_AFTER_INSTANCE_REMOVED_SECONDS_KEY, COOLDOWN_AFTER_INSTANCE_REMOVED_SECONDS_DEFAULT);
    }
    
    /**
     * Sets the minimum scale capacity that the @{link {@link ProcessingUnit}
     * is deployed with.
     * 
     * The result of a scaling rule result that decreases capacity will never
     * breach the minimum scale capacity.  
     */
    public void setMinCapacity(CapacityRequirementsConfig minCapacity) {
       properties.putMap(MIN_CAPACITY_KEY_PREFIX, minCapacity.getProperties());
    }

    public CapacityRequirementsConfig getMinCapacity() {
        return new CapacityRequirementsConfig(properties.getMap(MIN_CAPACITY_KEY_PREFIX, EMPTY_CAPACITY));
    }

    /**
     * Sets the maximum scale capacity that the @{link {@link ProcessingUnit}
     * is deployed with.
     * 
     * The result of a scaling rule result that increases capacity will never
     * breach the maximum scale capacity.  
     */
    public void setMaxCapacity(CapacityRequirementsConfig maxCapacity) {
        properties.putMap(MAX_CAPACITY_KEY_PREFIX, maxCapacity.getProperties());
    }

    public void setRules(AutomaticCapacityScaleRuleConfig[] rules) {
        for (int i = 0 ; i < rules.length ; i++) {
            properties.putMap(getRulesKeyPrefix(i), rules[i].getProperties());
        }
    }
    
    public AutomaticCapacityScaleRuleConfig[] getRules() {
        
        List<AutomaticCapacityScaleRuleConfig> rules = new ArrayList<AutomaticCapacityScaleRuleConfig>();
        
        for (int i = 0;; i++) {
            Map<String, String> ruleProperties = properties.getMap(getRulesKeyPrefix(i), null);
            if (ruleProperties == null) {
                // no more rules
                break;
            }
            rules.add(new AutomaticCapacityScaleRuleConfig(ruleProperties));
        }
        
        return rules.toArray(new AutomaticCapacityScaleRuleConfig[rules.size()]);
    }

    private String getRulesKeyPrefix(int i) {
        return RULE_KEY + i +".";
    }

    public CapacityRequirementsConfig getMaxCapacity() {
        return new CapacityRequirementsConfig(properties.getMap(MAX_CAPACITY_KEY_PREFIX, EMPTY_CAPACITY));
    }
    
    public void setInitialCapacity(CapacityRequirementsConfig initialCapacity) {
        properties.putMap(INITIAL_CAPACITY_KEY_PREFIX, initialCapacity.getProperties());
    }
    
    public CapacityRequirementsConfig getInitialCapacity() {
        return new CapacityRequirementsConfig(properties.getMap(INITIAL_CAPACITY_KEY_PREFIX, EMPTY_CAPACITY));
    }
    
    @Override
    public String toString() {
        return this.properties.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        AutomaticCapacityScaleConfig other = (AutomaticCapacityScaleConfig) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.properties.getProperties());
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.properties = new StringProperties((Map<String,String>)in.readObject());
    }

}
