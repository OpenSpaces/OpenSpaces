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

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsPerZonesConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;
import org.openspaces.admin.zone.config.AnyZonesConfig;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaPolicy;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils;
import org.openspaces.grid.gsm.autoscaling.AutomaticCapacityCooldownValidator;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
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
    private AutomaticCapacityScaleConfig config;
    private AutomaticCapacityCooldownValidator cooldownValidator;
    
    // events state
    private ScaleStrategyProgressEventState autoScalingEventState;
    private CapacityRequirementsPerZones enforcedCapacityRequirementsPerZones;
    
    @Override
    public void afterPropertiesSet() {
        
        super.afterPropertiesSet();
                
        this.config = new AutomaticCapacityScaleConfig(super.getProperties());
        
        validateConfig();
        
        
        this.cooldownValidator = new AutomaticCapacityCooldownValidator();
        this.cooldownValidator.setCooldownAfterInstanceAdded(config.getCooldownAfterScaleOutSeconds(), TimeUnit.SECONDS);
        this.cooldownValidator.setCooldownAfterInstanceRemoved(config.getCooldownAfterScaleSeconds(), TimeUnit.SECONDS);
        this.cooldownValidator.setProcessingUnit(getProcessingUnit());
        CapacityRequirementsConfig initialCapacity = config.getInitialCapacity();
        if (initialCapacity == null) {   
            initialCapacity = config.getMinCapacity();
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Initial capacity is not set. defaulting to minimum capacity:"+ config.getMinCapacity());
            }
        }
        else {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Initial capacity is set to :"+ initialCapacity);
            }
        }
        
        autoScalingEventState = 
            new ScaleStrategyProgressEventState(
                getEventsStore(), 
                isUndeploying(),
                getProcessingUnit().getName(), 
                DefaultElasticAutoScalingProgressChangedEvent.class,
                DefaultElasticAutoScalingFailureEvent.class);

        //inject initial manual scale capacity
        super.setPlannedCapacity(initialCapacity);
        super.setScaleStrategyConfig(config);
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("isGridServiceAgentZonesAware="+isGridServiceAgentZonesAware());
        }
        
        enablePuStatistics();
    }

    private void validateConfig() {
        
        validateRulesConfig();
        
        CapacityRequirements minCapacityRequirements = config.getMinCapacity().toCapacityRequirements();
        if (minCapacityRequirements == null) {
            throw new BeanConfigurationException("Minimum capacity requirements is undefined");
        }
        
        CapacityRequirements maxCapacityRequirements = config.getMaxCapacity().toCapacityRequirements();
        if (maxCapacityRequirements == null) {
            throw new BeanConfigurationException("Maximum capacity requirements is undefined");
        }
        
        if (minCapacityRequirements.greaterThan(maxCapacityRequirements)) {
            throw new BeanConfigurationException("Maximum capacity (" + maxCapacityRequirements
                    + ") is less than minimum capacity (" + minCapacityRequirements + ")");
        }
        
        CapacityRequirementsConfig initialCapacity = config.getInitialCapacity();
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
        return config;
    }

    @Override
    protected void recoverStateOnEsmStart() throws SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException, MachinesSlaEnforcementInProgressException {
        super.recoverStateOnEsmStart();
        CapacityRequirementsPerZones recoveredCapacity = super.getAllocatedCapacity();
        if (!recoveredCapacity.equalsZero()) {
            // the ESM was restarted, as evident by the recovered capacity
            // in which case we need to overwrite the initial capacity with the recovered capacity
            super.setPlannedCapacity(recoveredCapacity);
        }
    }
    
    @Override
    protected void enforceSla() throws SlaEnforcementInProgressException {
        
        SlaEnforcementInProgressException pendingException=null;
        final CapacityRequirementsPerZones capacityRequirements = super.getCapacityRequirementConfig().toCapacityRequirementsPerZones();
        
        try {
            super.enforcePlannedCapacity();
            enforcedCapacityRequirementsPerZones = capacityRequirements;
            // no exception means that manual scale is complete.
        }
        catch (RebalancingSlaEnforcementInProgressException e) {
            // do not run autoscaling algorithm if instances are changing
            throw e;
        }
        catch (ContainersSlaEnforcementInProgressException e) {
            // do not run autoscaling algorithm since GSM may already start deploying new instances
            throw e;
        }
        catch (SlaEnforcementInProgressException e) {
            
            if (enforcedCapacityRequirementsPerZones == null) {
                // no prev capacityRequirements to work with
                throw e;
            }
            
            // no effect on instances yet... proceed with auto scaling rules
            // The reasoning is that it may take a long time for machines to start
            // and during that time the capacity requirements may need to change
            pendingException = e;
        }
        
        CapacityRequirementsPerZones newCapacityRequirementsPerZones;
        
        try {
            //make sure that we are not in the cooldown period
            //Notice this check is performed after PU is INTACT, meaning the USM is already started
            //@see DefaultAdmin#degradeUniversalServiceManagerProcessingUnitStatus()
            cooldownValidator.validate();
        
            //enforce auto-scaling SLA
            //based on the last enforced SLA, the reason is that the monitored data reflects the last enforced SLA
            //and it could have changed since then (happens when pendingException != null)
            
            //TODO: Support multizone
            newCapacityRequirementsPerZones = enforceAutoScalingSla(enforcedCapacityRequirementsPerZones);
        }
        catch (SlaEnforcementInProgressException e) {
            if (pendingException != null) {
                // throw pending exception of previous manual scale capacity.
                // otherwise it could be lost when calling it again.
                throw pendingException;
            }
            throw e;
        }
        
        if (!newCapacityRequirementsPerZones.equals(capacityRequirements)) {
            super.setPlannedCapacity(new CapacityRequirementsPerZonesConfig(newCapacityRequirementsPerZones));
            if (pendingException != null) {
                // throw pending exception of previous manual scale capacity.
                // otherwise it could be lost when calling it again.
                throw pendingException;
            }
            // enforce new capacity requirements as soon as possible.
            super.enforcePlannedCapacity();
        }
        
        if (pendingException != null) {
            // throw pending exception of previous manual scale capacity, so it won't be lost.
            throw pendingException;
        }
    }

    private CapacityRequirementsPerZones enforceAutoScalingSla(final CapacityRequirementsPerZones capacityRequirementsPerZones) throws AutoScalingSlaEnforcementInProgressException {
        //TODO: Support multi-zone
        final CapacityRequirements capacityRequirements = capacityRequirementsPerZones.getZonesCapacityOrZero(new AnyZonesConfig());
        final CapacityRequirements newCapacityRequirements = enforceAutoScalingSla(capacityRequirements);
        return  new CapacityRequirementsPerZones().add(new AnyZonesConfig(), newCapacityRequirements);
    }

    private CapacityRequirements enforceAutoScalingSla(final CapacityRequirements capacityRequirements)
            throws AutoScalingSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing automatic scaling SLA.");
        }
        
        final AutoScalingSlaPolicy sla = new AutoScalingSlaPolicy();
        sla.setCapacityRequirements(capacityRequirements);
        sla.setMaxCapacity(config.getMaxCapacity().toCapacityRequirements());
        sla.setMinCapacity(config.getMinCapacity().toCapacityRequirements());
        sla.setRules(config.getRules());
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

    private void validateRulesConfig() {
        for (AutomaticCapacityScaleRuleConfig rule : config.getRules()) {
            if (!rule.getLowThresholdBreachedDecrease().toCapacityRequirements().equalsZero() && 
                !rule.getHighThresholdBreachedIncrease().toCapacityRequirements().equalsZero()) {
                try {
                    if (AutoScalingSlaUtils.compare(rule.getLowThreshold(),rule.getHighThreshold()) > 0) {
                        throw new BeanConfigurationException("Low threshold (" + rule.getLowThreshold() + ") cannot be higher than high threshold (" + rule.getHighThreshold() +")");
                    }
                } catch (NumberFormatException e) {
                    throw new BeanConfigurationException("Failed to compare low threshold (" + rule.getLowThreshold() + ") and high threshold (" + rule.getHighThreshold() +")",e);
                }
            }
        }
    }
    
    private void enablePuStatistics() {
        int maxNumberOfSamples = 1;
        for (AutomaticCapacityScaleRuleConfig rule : config.getRules()) {
            
            TimeWindowStatisticsConfig timeWindowStatistics = rule.getStatistics().getTimeWindowStatistics();

            maxNumberOfSamples = 
                    Math.max(
                       maxNumberOfSamples,
                       timeWindowStatistics.getMaxNumberOfSamples(
                               config.getStatisticsPollingIntervalSeconds(), 
                               TimeUnit.SECONDS));
            
            getProcessingUnit().addStatisticsCalculation(rule.getStatistics());
                               
        }
        getLogger().info("Start statistics polling for " + getProcessingUnit().getName() + " to " + config.getStatisticsPollingIntervalSeconds() + " seconds, history size is " + maxNumberOfSamples + " samples.");
        getProcessingUnit().setStatisticsHistorySize(maxNumberOfSamples);
        getProcessingUnit().setStatisticsInterval(config.getStatisticsPollingIntervalSeconds(), TimeUnit.SECONDS);
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
