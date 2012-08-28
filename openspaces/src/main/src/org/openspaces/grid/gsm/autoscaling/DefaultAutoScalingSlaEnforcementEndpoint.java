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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingStatisticsFormatException;
import org.openspaces.grid.gsm.autoscaling.exceptions.ReachedMaximumCapacityAutoScalingException;
import org.openspaces.grid.gsm.autoscaling.exceptions.RulesConflictAutoScalingException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * @author itaif
 * @since 9.0.0
 */
public class DefaultAutoScalingSlaEnforcementEndpoint implements AutoScalingSlaEnforcementEndpoint {

    private final Log logger;
    private InternalProcessingUnit pu;
    private CapacityRequirements newCapacity = null;
    
    public DefaultAutoScalingSlaEnforcementEndpoint(ProcessingUnit pu) {
        this.pu = (InternalProcessingUnit)pu;
        this.logger = 
            new LogPerProcessingUnit(
                new SingleThreadedPollingLog(
                        LogFactory.getLog(this.getClass())), 
                pu);
    }
    
    @Override
    public CapacityRequirements getNewCapacityRequirements() {
        return newCapacity;
    }
    
    @Override
    public void enforceSla(AutoScalingSlaPolicy sla) throws AutoScalingSlaEnforcementInProgressException {

        if (sla == null) {
            throw new IllegalArgumentException("SLA cannot be null");
        }
        
        sla.validate();
                
        this.newCapacity = enforceSlaInternal(sla);
    }
    
    private CapacityRequirements enforceSlaInternal(AutoScalingSlaPolicy sla) throws AutoScalingSlaEnforcementInProgressException {
        
        Map<ProcessingUnitStatisticsId, Object> statistics = pu.getStatistics().getStatistics();
                
        Map<AutomaticCapacityScaleRuleConfig,Object> valuesBelowLowThresholdPerRule = new HashMap<AutomaticCapacityScaleRuleConfig, Object>();
        Map<AutomaticCapacityScaleRuleConfig,Object> valuesAboveHighThresholdPerRule = new HashMap<AutomaticCapacityScaleRuleConfig, Object>();
        
        // check thresholds for every rule in the sla
        if (logger.isTraceEnabled()) {
            int numberOfRules = sla.getRules().length;
            if (numberOfRules == 0) {
                logger.trace("No scaling rules defined");
            }
            else {
                logger.trace(numberOfRules + " scaling rules defined");
            }
        }
        
        // each rule in this sla with recieve the current sla zone
        ZonesConfig zonesConfig = sla.getZonesConfig(); 
        
        for (AutomaticCapacityScaleRuleConfig rule: sla.getRules()) {

            // desired statistics to calculate for this rule
            ProcessingUnitStatisticsId statisticsId = rule.getStatistics();
            
            zonesConfig.validate();
            statisticsId.setAgentZones(zonesConfig);
            Object value = AutoScalingSlaUtils.getStatisticsValue(pu, statistics, statisticsId);
            
            if (logger.isTraceEnabled()) {
                logger.trace("Checking value " + value + " against thresholds of scaling rule " + rule);
            }
            
            boolean belowLowThreshold = isBelowLowThreshold(rule, value);
            boolean aboveHighThreshold = isAboveHighThreshold(rule, value);
            
            if (belowLowThreshold) {
                if (logger.isInfoEnabled()) {
                    logger.info("Low threshold breached: " + value + " is less than " + rule.getLowThreshold() + ". Scaling rule: " + rule);
                }
                valuesBelowLowThresholdPerRule.put(rule,value);
            }
            
            if (aboveHighThreshold) {
                if (logger.isInfoEnabled()) {
                    logger.info("High threshold breached: " + value + " is greater than " + rule.getHighThreshold() + ". Scaling rule: " + rule);
                }
                valuesAboveHighThresholdPerRule.put(rule,value);
            }
            
            if (logger.isDebugEnabled()) {
                if (!belowLowThreshold && !aboveHighThreshold) {
                    logger.debug("Value is within thresholds for rule " + rule);
                }
            }
        }

        CapacityRequirements existingCapacity = sla.getCapacityRequirements();
        
        //TODO: Perform forward looking (linear extrapolation of samples) to prevent case of rules resonance
        //      one rule scaling out another scaling down
        if (!valuesAboveHighThresholdPerRule.isEmpty() && !valuesBelowLowThresholdPerRule.isEmpty()) {
            throw new RulesConflictAutoScalingException(pu, valuesBelowLowThresholdPerRule, valuesAboveHighThresholdPerRule);
        }
        else if (!valuesAboveHighThresholdPerRule.isEmpty()) {
            
            CapacityRequirements minimunHighThresholdBreachedIncrease = getMinimumRuleChange(valuesAboveHighThresholdPerRule, true);
            CapacityRequirements newCapacity = existingCapacity.add(minimunHighThresholdBreachedIncrease);
            CapacityRequirements maxCapacity = sla.getMaxCapacity();
            
            if (newCapacity.greaterThan(maxCapacity)) {
                //apply max capacity restriction
                CapacityRequirements correctedNewCapacity = newCapacity.min(maxCapacity);
                if (correctedNewCapacity.equals(existingCapacity)) {
                    throw new ReachedMaximumCapacityAutoScalingException(pu, existingCapacity, newCapacity, maxCapacity );
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot increase capacity from " + existingCapacity + " to " + newCapacity + " since it breaches maximum of " + maxCapacity +".");
                }
                newCapacity = correctedNewCapacity;
            }
            
            if (!newCapacity.greaterThan(existingCapacity)) {
                throw new IllegalStateException("Expected " + newCapacity + " to be bigger than " + existingCapacity +  " due to the increase by " + minimunHighThresholdBreachedIncrease);
            }
            
            if (logger.isInfoEnabled()) {
                logger.info("Increasing capacity from " + existingCapacity + " to " + newCapacity);
            }
            return newCapacity;
        }
        else if (!valuesBelowLowThresholdPerRule.isEmpty()) {
            
            CapacityRequirements minimunLowThresholdBreachedDecrease = getMinimumRuleChange(valuesBelowLowThresholdPerRule, false);
            CapacityRequirements newCapacity = existingCapacity.subtractOrZero(minimunLowThresholdBreachedDecrease);
            CapacityRequirements minCapacity = sla.getMinCapacity();
            
            if (minCapacity.greaterThan(newCapacity)) {
                // apply min capacity restriction
                // do not throw exception
                CapacityRequirements correctedNewCapacity = newCapacity.max(minCapacity);
                if (existingCapacity.equals(newCapacity)) {
                    // do not throw exception since this is a common use case.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot decrease capacity below minimum of " + minCapacity +". Otherwise would have decreased capacity to " + newCapacity);
                    }
                }
                newCapacity = correctedNewCapacity;
            }
            if (!existingCapacity.equals(newCapacity)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Decreasing capacity from " + existingCapacity + " to " + newCapacity);
                }
                return newCapacity;
            }
        }

        return existingCapacity;
    }

    private CapacityRequirements getMinimumRuleChange(
            Map<AutomaticCapacityScaleRuleConfig, Object> valuesAboveHighThresholdPerRule, boolean increase) {
        
        final Set<AutomaticCapacityScaleRuleConfig> automaticCapacityScaleRuleConfigs = valuesAboveHighThresholdPerRule.keySet();
        if (automaticCapacityScaleRuleConfigs.isEmpty()) {
            throw new IllegalStateException("automaticCapacityScaleRuleConfigs cannot be empty");
        }
        final Iterator<AutomaticCapacityScaleRuleConfig> iterator = automaticCapacityScaleRuleConfigs.iterator();
        CapacityRequirements minimunChange = null;
        while (iterator.hasNext()) {
            final AutomaticCapacityScaleRuleConfig ruleConfig = iterator.next();
            CapacityRequirements change; 
            if (increase) {
                change = ruleConfig.getHighThresholdBreachedIncrease().toCapacityRequirements();
                if (change.equalsZero()) {
                    throw new IllegalStateException("highThresholdIncrease cannot be zero in scale rule " + ruleConfig);
                }
            }
            else {
                change = ruleConfig.getLowThresholdBreachedDecrease().toCapacityRequirements();
                if (change.equalsZero()) {
                    throw new IllegalStateException("lowThresholdIncrease cannot be zero in scale rule " + ruleConfig);
                }
            }
            
            if (minimunChange == null) {
                minimunChange = change;
            }
            else {
                minimunChange = minimunChange.min(change);
            }
        }
        if (minimunChange.equalsZero()) {
            throw new IllegalStateException("minimunCapcityHighThresholdIncreaseRequirements cannot be zero");
        }
        return minimunChange;
    }
    
    public boolean isBelowLowThreshold(AutomaticCapacityScaleRuleConfig rule, Object value) 
            throws AutoScalingSlaEnforcementInProgressException {
        try {
            return !rule.getLowThresholdBreachedDecrease().toCapacityRequirements().equalsZero() &&
                   AutoScalingSlaUtils.compare(rule.getLowThreshold(), value) > 0;
        }
        catch (final NumberFormatException e) {
            throw new AutoScalingStatisticsFormatException(pu, value ,rule.getLowThreshold() ,e);
        }
    }
    
    public boolean isAboveHighThreshold(AutomaticCapacityScaleRuleConfig rule, Object value) 
            throws AutoScalingSlaEnforcementInProgressException {
        
        try {
            return !rule.getHighThresholdBreachedIncrease().toCapacityRequirements().equalsZero() &&
                   AutoScalingSlaUtils.compare(rule.getHighThreshold(), value) < 0;
        }
        catch (final NumberFormatException e) {
            throw new AutoScalingStatisticsFormatException(pu, value ,rule.getHighThreshold() ,e);
        }
    }
}
