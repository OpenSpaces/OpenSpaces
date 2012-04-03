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
package org.openspaces.grid.gsm.strategy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaPolicy;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils;
import org.openspaces.grid.gsm.autoscaling.AutomaticCapacityCooldownValidator;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
+ * The business logic that scales an elastic processing unit based on the specified
+ * {@link AutomaticCapacityScaleConfig}
+ * 
+ * @author itaif
+ * @since 9.0.0
+ */
public class AutomaticCapacityScaleStrategyBean extends AbstractCapacityScaleStrategyBean
        implements AutoScalingSlaEnforcementEndpointAware {

    private AutoScalingSlaEnforcementEndpoint autoScalingEndpoint;

    @Override
    public void setAutoScalingSlaEnforcementEndpoint(AutoScalingSlaEnforcementEndpoint endpoint) {
        this.autoScalingEndpoint = endpoint;
    }
    
    // created by afterPropertiesSet()
    private AutomaticCapacityScaleConfig automaticCapacity;
    private AutomaticCapacityCooldownValidator cooldownValidator;
    
    // events state
    private ScaleStrategyProgressEventState autoScalingEventState;
    
    @Override
    public void afterPropertiesSet() {
        
        super.afterPropertiesSet();
                
        validateConfig();
        
        this.automaticCapacity = new AutomaticCapacityScaleConfig(super.getProperties());
        
        this.cooldownValidator = new AutomaticCapacityCooldownValidator();
        this.cooldownValidator.setCooldownAfterInstanceAdded(automaticCapacity.getCooldownAfterInstanceAddedSeconds(), TimeUnit.SECONDS);
        this.cooldownValidator.setCooldownAfterInstanceRemoved(automaticCapacity.getCooldownAfterInstanceRemovedSeconds(), TimeUnit.SECONDS);
        
        CapacityRequirementsConfig initialCapacity = automaticCapacity.getInitialCapacity();
        if (initialCapacity == null) {   
            initialCapacity = automaticCapacity.getMinCapacity();
        }
        
        autoScalingEventState = 
            new ScaleStrategyProgressEventState(
                getEventsStore(), 
                isUndeploying(),
                getProcessingUnit().getName(), 
                DefaultElasticAutoScalingProgressChangedEvent.class,
                DefaultElasticAutoScalingFailureEvent.class);

        //inject initial manual scale capacity
        super.setCapacityRequirementConfig(initialCapacity);
        super.setScaleStrategyConfig(automaticCapacity);
        
        enablePuStatistics();
    }

    private void validateConfig() {
        
        CapacityRequirements minCapacityRequirements = automaticCapacity.getMinCapacity().toCapacityRequirements();
        if (minCapacityRequirements == null) {
            throw new BeanConfigurationException("Minimum capacity requirements is undefined");
        }
        
        CapacityRequirements maxCapacityRequirements = automaticCapacity.getMaxCapacity().toCapacityRequirements();
        if (maxCapacityRequirements == null) {
            throw new BeanConfigurationException("Maximum capacity requirements is undefined");
        }
        
        if (minCapacityRequirements.greaterThan(maxCapacityRequirements)) {
            throw new BeanConfigurationException("Maximum capacity (" + maxCapacityRequirements
                    + ") is less than minimum capacity (" + minCapacityRequirements + ")");
        }
        
        CapacityRequirementsConfig initialCapacity = automaticCapacity.getInitialCapacity();
        if (initialCapacity != null) {
            CapacityRequirements initialCapacityRequirements = initialCapacity.toCapacityRequirements();
            
            if (minCapacityRequirements.greaterThan(initialCapacityRequirements)) {
                throw new BeanConfigurationException("Initial capacity (" + initialCapacityRequirements
                        + ") is less than minimum capacity (" + minCapacityRequirements + ")");
            }
            
            if (initialCapacityRequirements.greaterThan(maxCapacityRequirements)) {
                throw new BeanConfigurationException("Initial capacity (" + initialCapacityRequirements
                        + ") is greater than maximum capacity (" + minCapacityRequirements + ")");
            }
        }
    }
    
    @Override
    public void destroy() {
        disablePuStatistics();
        super.destroy();
    }
    
    @Override
    public ScaleStrategyConfig getConfig() {
        return automaticCapacity;
    }

    
    @Override
    protected void enforceSla() throws SlaEnforcementInProgressException {
        
        super.enforceCapacityRequirement(); //enforces the last call to #setCapacityRequirementConfig
        // no exception means that manual scale is complete. 
        
        //make sure that we are not in the cooldown period
        cooldownValidator.validate(getInstancesUids());

        //enforce auto-scaling SLA
        final CapacityRequirements capacityRequirements = super.getCapacityRequirementConfig().toCapacityRequirements();
        final CapacityRequirements newCapacityRequirements = enforceAutoScalingSla(capacityRequirements);
        if (!newCapacityRequirements.equals(capacityRequirements)) {
            super.setCapacityRequirementConfig(new CapacityRequirementsConfig(newCapacityRequirements));
            super.enforceCapacityRequirement();
        }
    }


    private Set<String> getInstancesUids() {
        Set<String> instanceUids = new HashSet<String>();
        for (ProcessingUnitInstance instance : super.getProcessingUnit()) {
            instanceUids.add(instance.getUid());
        }
        return instanceUids;
    }
    
    private CapacityRequirements enforceAutoScalingSla(final CapacityRequirements capacityRequirements)
            throws AutoScalingSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing automatic scaling SLA.");
        }
        
        final AutoScalingSlaPolicy sla = new AutoScalingSlaPolicy();
        sla.setCapacityRequirements(capacityRequirements);
        sla.setHighThresholdBreachedIncrease(getCapacityChangeOnBreach());
        sla.setLowThresholdBreachedDecrease(getCapacityChangeOnBreach());
        sla.setMaxCapacity(automaticCapacity.getMaxCapacity().toCapacityRequirements());
        sla.setMinCapacity(automaticCapacity.getMinCapacity().toCapacityRequirements());
        sla.setRules(automaticCapacity.getRules());
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Automatic Scaling SLA Policy: " + sla);
        }
                
        try {
            autoScalingEndpoint.enforceSla(sla);
            autoScalingCompletedEvent();
        }
        catch (AutoScalingSlaEnforcementInProgressException e) {
            autoScalingInProgressEvent(e);
            throw e;
        }
        final CapacityRequirements newCapacityRequirements = autoScalingEndpoint.getNewCapacityRequirements();
        return newCapacityRequirements;
    }

    private CapacityRequirements getCapacityChangeOnBreach() {
        long containerMemory = getGridServiceContainerConfig().getMaximumMemoryCapacityInMB();
        return new CapacityRequirements(new MemoryCapacityRequirement(containerMemory));
    }

    private void enablePuStatistics() {
        int maxNumberOfSamples = 1;
        for (AutomaticCapacityScaleRuleConfig rule : automaticCapacity.getRules()) {
            try {
                if (AutoScalingSlaUtils.compare(rule.getLowThreshold(),rule.getHighThreshold()) > 0) {
                    throw new BeanConfigurationException("Low threshold (" + rule.getLowThreshold() + ") cannot be higher than high threshold (" + rule.getHighThreshold() +")");
                }
            } catch (NumberFormatException e) {
                throw new BeanConfigurationException("Failed to compare low threshold (" + rule.getLowThreshold() + ") and high threshold (" + rule.getHighThreshold() +")",e);
            }
            
            TimeWindowStatisticsConfig timeWindowStatistics = rule.getStatistics().getTimeWindowStatistics();

            maxNumberOfSamples = 
                    Math.max(
                       maxNumberOfSamples,
                       timeWindowStatistics.getMaxNumberOfSamples(
                               automaticCapacity.getStatisticsPollingIntervalSeconds(), 
                               TimeUnit.SECONDS));
            
            getProcessingUnit().addStatisticsCalculation(rule.getStatistics());
                               
        }
        getLogger().info("Start statistics polling for " + getProcessingUnit().getName() + " to " + automaticCapacity.getStatisticsPollingIntervalSeconds() + " seconds, history size is " + maxNumberOfSamples + " samples.");
        getProcessingUnit().setStatisticsHistorySize(maxNumberOfSamples);
        getProcessingUnit().setStatisticsInterval(automaticCapacity.getStatisticsPollingIntervalSeconds(), TimeUnit.SECONDS);
        getProcessingUnit().startStatisticsMonitor();
    }
    
    private void disablePuStatistics() {
        getProcessingUnit().stopStatisticsMonitor();
    }
    
    private void autoScalingCompletedEvent() {
        autoScalingEventState.enqueuProvisioningCompletedEvent();
    }

    private void autoScalingInProgressEvent(AutoScalingSlaEnforcementInProgressException e) {
        autoScalingEventState.enqueuProvisioningInProgressEvent(e);
    }
}
