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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
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
         
        for (AutomaticCapacityScaleRuleConfig rule: sla.getRules()) {

            ProcessingUnitStatisticsId statisticsId = rule.getStatistics();
            Object value = AutoScalingSlaUtils.getStatisticsValue(pu, statistics, statisticsId);
                        
            boolean belowLowThreshold = isBelowLowThreshold(rule, value);
            boolean aboveHighThreshold =isAboveHighThreshold(rule, value);
            
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
            CapacityRequirements newCapacity = existingCapacity.add(sla.getHighThresholdBreachedIncrease());
            CapacityRequirements maxCapacity = sla.getMaxCapacity();
            
            if (newCapacity.greaterThan(maxCapacity)) {
                //apply max capacity restriction
                CapacityRequirements correctedNewCapacity = newCapacity.min(maxCapacity);
                if (existingCapacity.equals(correctedNewCapacity)) {
                    throw new ReachedMaximumCapacityAutoScalingException(pu, existingCapacity, newCapacity, maxCapacity );
                }
                newCapacity = correctedNewCapacity;
            }
            if (logger.isInfoEnabled()) {
                logger.info("Increasing capacity from " + existingCapacity + " to " + newCapacity);
            }
            return newCapacity;
        }
        else if (!valuesBelowLowThresholdPerRule.isEmpty()) {
            CapacityRequirements newCapacity = existingCapacity.subtractOrZero(sla.getLowThresholdBreachedDecrease());
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
    
    public boolean isBelowLowThreshold(AutomaticCapacityScaleRuleConfig rule, Object value) 
            throws AutoScalingSlaEnforcementInProgressException {
        try {
            return AutoScalingSlaUtils.compare(rule.getLowThreshold(), value) > 0;
        }
        catch (final NumberFormatException e) {
            throw new AutoScalingStatisticsFormatException(pu, value ,rule.getLowThreshold() ,e);
        }
    }
    
    public boolean isAboveHighThreshold(AutomaticCapacityScaleRuleConfig rule, Object value) 
            throws AutoScalingSlaEnforcementInProgressException {
        
        try {
            return AutoScalingSlaUtils.compare(rule.getHighThreshold(), value) < 0;
        }
        catch (final NumberFormatException e) {
            throw new AutoScalingStatisticsFormatException(pu, value ,rule.getHighThreshold() ,e);
        }
    }
}
