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
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsPerZonesConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaPolicy;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils;
import org.openspaces.grid.gsm.autoscaling.AutomaticCapacityCooldownValidator;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingConfigConflictException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.autoscaling.exceptions.PerZoneAutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaHasChangedException;
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

    // created by afterPropertiesSet()
    private AutomaticCapacityScaleConfig config;
    private AutomaticCapacityCooldownValidator cooldownValidator;

    // events state
    private ScaleStrategyProgressEventState autoScalingEventState;
    private CapacityRequirementsPerZones lastEnforcedPlannedCapacity;

    @Override
    public void setAutoScalingSlaEnforcementEndpoint(AutoScalingSlaEnforcementEndpoint endpoint) {
        this.autoScalingEndpoint = endpoint;
    }


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

        getProcessingUnit().setStatisticsInterval(config.getStatisticsPollingIntervalSeconds(), TimeUnit.SECONDS);
        getProcessingUnit().startStatisticsMonitor();
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
            setPlannedCapacity(recoveredCapacity);
        }
    }
    
    @Override
    protected boolean setPlannedCapacity(CapacityRequirementsPerZonesConfig config) {
        boolean hasChanged = super.setPlannedCapacity(config);
        if (hasChanged) {
            enablePuStatistics();
        }
        return hasChanged;
    }

    @Override
    protected void enforceSla() throws SlaEnforcementInProgressException {

        PerZoneAutoScalingSlaEnforcementInProgressException pendingAutoscaleInProgressExceptions = new PerZoneAutoScalingSlaEnforcementInProgressException(getProcessingUnit(), "Multiple Exceptions");
        
        SlaEnforcementInProgressException pendingEnforcePlannedCapacityException = null;
        final CapacityRequirementsPerZones plannedCapacityRequirementsPerZones = super.getPlannedCapacity().toCapacityRequirementsPerZones();

        try {
            super.enforcePlannedCapacity();
            lastEnforcedPlannedCapacity = plannedCapacityRequirementsPerZones;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("enforcedCapacityRequirementsPerZones = " + plannedCapacityRequirementsPerZones);
            }
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
        catch (MachinesSlaHasChangedException e) {
            //start over, since the plan has changed
            throw e;
        } 
        catch (SlaEnforcementInProgressException e) {

            if (lastEnforcedPlannedCapacity == null) {
                // no prev capacityRequirements to work with
                throw e;
            }

            // no effect on instances yet... proceed with auto scaling rules
            // The reasoning is that it may take a long time for machines to start
            // and during that time the capacity requirements may need to change
            pendingEnforcePlannedCapacityException = e;
        }



        CapacityRequirementsPerZones newPlannedCapacity = new CapacityRequirementsPerZones();
        boolean capacityChangedDueToAutoScalingRules = false;

        // make sure that we are not in the cooldown period
        // Notice this check is performed after PU is INTACT, meaning the USM is already started
        // @see DefaultAdmin#degradeUniversalServiceManagerProcessingUnitStatus()
        cooldownValidator.validate();
 
        if (!isGridServiceAgentZonesAware()) {

            ZonesConfig defaultZones = null;
            try {

                defaultZones = getDefaultZones();

                final CapacityRequirements capacityForDefaultZone = lastEnforcedPlannedCapacity.getZonesCapacityOrZero(defaultZones);
                final CapacityRequirements maximumCapacityForDefaultZone = config.getMaxCapacity().toCapacityRequirements();
                final CapacityRequirements minimumCapacityForDefaultZone = config.getMinCapacity().toCapacityRequirements();
                
                Set<ZonesConfig> defaultZonesConfigs = new HashSet<ZonesConfig>();
                defaultZonesConfigs.add(getDefaultZones());
                final CapacityRequirements newCapacityForDefaultZone = enforceAutoScalingSla(capacityForDefaultZone, defaultZones, lastEnforcedPlannedCapacity, newPlannedCapacity, defaultZonesConfigs);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("newCapacityForDefaultZone = " + newCapacityForDefaultZone);
                }
                if (!newCapacityForDefaultZone.equals(capacityForDefaultZone)) { // new capacity may zero if minimum capacity is zero
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("newCapacityForDefaultZone = " + newCapacityForDefaultZone + " is not zero and is different from old capacity requirements = " + capacityForDefaultZone + " adding capcity requirement = " + newCapacityForDefaultZone + " to newCapacityRequirementsPerZones = " + newPlannedCapacity);           
                    }
                    newPlannedCapacity = newPlannedCapacity.set(defaultZones, newCapacityForDefaultZone);
                    capacityChangedDueToAutoScalingRules = true;
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("newPlannedCapacity = " + newPlannedCapacity);
                    }
                }



            } catch (AutoScalingSlaEnforcementInProgressException e) {
                if (pendingEnforcePlannedCapacityException != null) {
                    // throw pending exception of previous manual scale capacity.
                    // otherwise it could be lost when calling it again.
                    throw pendingEnforcePlannedCapacityException;
                }
                throw e;
            }

        } else {

            Set<ZonesConfig> zoness = lastEnforcedPlannedCapacity.getZones();
            // enforce SLA for each zone separately.
            for (ZonesConfig zones : zoness) {

                final CapacityRequirements capacityForZones = lastEnforcedPlannedCapacity.getZonesCapacityOrZero(zones);
                
                // enforce auto-scaling SLA for a specific zone
                // based on the last enforced SLA, the reason is that the monitored data reflects the last enforced SLA
                // and it could have changed since then (happens when pendingException != null)
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("enforcing auto scaling sla for zones config = " + zones + ". current allocated capacity for these zones is " + capacityForZones);
                }
                
                try {

                    
                    final CapacityRequirements newCapacityForZones = enforceAutoScalingSla(capacityForZones, zones, lastEnforcedPlannedCapacity, newPlannedCapacity, zoness);

                    if (!newCapacityForZones.equals(capacityForZones)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("capacity for zones " + zones + " has changed from " + capacityForZones + " to " + newCapacityForZones);
                        }
                        capacityChangedDueToAutoScalingRules = true;
                        // new capacity may zero if minimum capacity per zone is zero
                        newPlannedCapacity = newPlannedCapacity.set(zones, newCapacityForZones);
                    }

                } catch (AutoScalingSlaEnforcementInProgressException e) {
                    // enforce last capacity for zone that threw the exception
                    newPlannedCapacity.set(zones, capacityForZones);
                    
                    // exception in one zone should not influence running autoscaling in another zone.
                    // save exceptions for later handling
                    pendingAutoscaleInProgressExceptions.addReason(zones, e);
                }
            }
        }        

        // no need to call AbstractCapacityScaleStrategyBean#enforePlannedCapacity if no capacity change is needed.
        if (capacityChangedDueToAutoScalingRules) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("planned capacity has changed to " + newPlannedCapacity);
            }  
            setPlannedCapacity(new CapacityRequirementsPerZonesConfig(newPlannedCapacity));
            if (pendingEnforcePlannedCapacityException != null) {
                // throw pending exception of previous manual scale capacity.
                // otherwise it could be lost when calling it again.
                throw pendingEnforcePlannedCapacityException;
            }
            // enforce new capacity requirements as soon as possible.
            super.enforcePlannedCapacity();
        }

        if (pendingEnforcePlannedCapacityException != null) {
            // throw pending exception of previous manual scale capacity, so it won't be lost.
            throw pendingEnforcePlannedCapacityException;
        }
        if (pendingAutoscaleInProgressExceptions.hasReason()) {
            // exceptions during autoscaling sla enforcement per zone
            throw pendingAutoscaleInProgressExceptions;
        }
    }


    /**
     * @param zoness 
     * @return
     */
    private CapacityRequirements getMinimumCapacity(CapacityRequirementsPerZones lastEnforcedPlannedCapacity, CapacityRequirementsPerZones newPlannedCapacityPerZones, ZonesConfig zones, Set<ZonesConfig> zoness) {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("calculating minimum capacity for zone = " + zones);
        }
        CapacityRequirements minimumRequierements = config.getMinCapacity().toCapacityRequirements(); // initial
        CapacityRequirements minimumCapacityRequirementsPerZone = config.getMinCapacityPerZone().toCapacityRequirements();

        for (ZonesConfig otherZone : zoness) {
            if (!zones.equals(otherZone)) {
                CapacityRequirements otherLastEnforced = lastEnforcedPlannedCapacity.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherNewPlanned = newPlannedCapacityPerZones.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherMinimumCapacity = null;
                if (otherNewPlanned.equalsZero()) {
                    // autoscaling did not calculate new capacity for 'otherZone' yet.
                    otherMinimumCapacity = minimumCapacityRequirementsPerZone.max(minimumCapacityRequirementsPerZone);
                } else { 
                    otherMinimumCapacity = otherLastEnforced.min(otherNewPlanned).max(minimumCapacityRequirementsPerZone);
                }
                minimumRequierements = minimumRequierements.subtractOrZero(otherMinimumCapacity);
            }       
        }
        // enforce minimum capacity per zone to be at least the pre-defince minimum capacity per zone
        minimumRequierements = minimumRequierements.max(minimumCapacityRequirementsPerZone);
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("minimum capacity for zone = " + zones + " calculation result was " + minimumRequierements);
        }
        return minimumRequierements; 
    }


    /**
     * Each zones' maximum capacity is based on the total maximum minus the capacity of other zones.
     * @return the maximum capacity per zone, given the last enforced sla
     */
    private CapacityRequirements getMaximumCapacity(CapacityRequirementsPerZones lastEnforcedPlannedCapacity, CapacityRequirementsPerZones newPlannedCapacityPerZones, ZonesConfig zones, Set<ZonesConfig> zoness) {
        
        CapacityRequirements maximumCapacity = config.getMaxCapacity().toCapacityRequirements(); // initial
        CapacityRequirements maximumCapacityRequirementsPerZone = config.getMaxCapacityPerZone().toCapacityRequirements();

        for (ZonesConfig otherZone : zoness) {
            if (!zones.equals(otherZone)) {
                CapacityRequirements otherLastEnforced = lastEnforcedPlannedCapacity.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherNewPlanned = newPlannedCapacityPerZones.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherMaximumCapacity = otherLastEnforced.max(otherNewPlanned).min(maximumCapacityRequirementsPerZone);
                maximumCapacity = maximumCapacity.subtractOrZero(otherMaximumCapacity); 
            }       
        }
        maximumCapacity = maximumCapacity.min(maximumCapacityRequirementsPerZone);
        return maximumCapacity; 
    }
    
    private CapacityRequirements enforceAutoScalingSla(final CapacityRequirements capacity , ZonesConfig zones, CapacityRequirementsPerZones lastEnforcedCapacityRequirementsPerZone, 
            CapacityRequirementsPerZones newCapacityRequirementsPerZone, Set<ZonesConfig> zoness)
            throws AutoScalingSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing automatic scaling SLA.");
        }

        final AutoScalingSlaPolicy sla = new AutoScalingSlaPolicy();
        sla.setCapacityRequirements(capacity);

        CapacityRequirements minimumCapacity = null;
        CapacityRequirements maximumCapacity = null;
        if (isGridServiceAgentZonesAware()) {
            maximumCapacity = getMaximumCapacity(lastEnforcedCapacityRequirementsPerZone, newCapacityRequirementsPerZone, zones, zoness);
            minimumCapacity = getMinimumCapacity(lastEnforcedPlannedCapacity, newCapacityRequirementsPerZone, zones, zoness);            
        } else {
            maximumCapacity = config.getMaxCapacity().toCapacityRequirements();
            minimumCapacity = config.getMinCapacity().toCapacityRequirements();
        }
        
        // for now we assume these values are already given as per zone.
        sla.setMaxCapacity(maximumCapacity);
        sla.setMinCapacity(minimumCapacity);
        sla.setRules(config.getRules());
        sla.setZonesConfig(zones);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Automatic Scaling SLA Policy: " + sla);
        }

        try {
            
            if (!maximumCapacity.greaterOrEquals(minimumCapacity)) {
                throw new AutoScalingConfigConflictException(getProcessingUnit(), minimumCapacity, maximumCapacity, 
                        (ExactZonesConfig) zones, lastEnforcedCapacityRequirementsPerZone, newCapacityRequirementsPerZone);
            }
            
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

        getLogger().info("enabling pu statistics for processing unit " + getProcessingUnit().getName());

        // for each rule, add the statistics to calculate
        for (AutomaticCapacityScaleRuleConfig rule : config.getRules()) {

            ProcessingUnitStatisticsId statisticsId = rule.getStatistics();
            TimeWindowStatisticsConfig timeWindowStatistics = statisticsId.getTimeWindowStatistics();

            if (!isGridServiceAgentZonesAware()) {   
                ProcessingUnitStatisticsId id = new ProcessingUnitStatisticsIdConfigurer()
                    .agentZones(getDefaultZones())
                    .instancesStatistics(statisticsId.getInstancesStatistics())
                    .metric(statisticsId.getMetric())
                    .monitor(statisticsId.getMetric())
                    .timeWindowStatistics(statisticsId.getTimeWindowStatistics())
                    .create();
                
                maxNumberOfSamples = 
                        Math.max(
                                maxNumberOfSamples,
                                timeWindowStatistics.getMaxNumberOfSamples(
                                        config.getStatisticsPollingIntervalSeconds(), 
                                        TimeUnit.SECONDS));
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("adding statistics calculation : " + statisticsId);                        
                }
                

                getProcessingUnit().addStatisticsCalculation(id);
            } else {
                Set<ZonesConfig> plannedZones = super.getPlannedZones();
                getLogger().info("plannedZones = " + plannedZones);
                    for (ZonesConfig zones : plannedZones) {
                        zones.validate();
                        ProcessingUnitStatisticsId id = new ProcessingUnitStatisticsIdConfigurer()
                            .agentZones(zones)
                            .instancesStatistics(statisticsId.getInstancesStatistics())
                            .metric(statisticsId.getMetric())
                            .monitor(statisticsId.getMetric())
                            .timeWindowStatistics(statisticsId.getTimeWindowStatistics())
                            .create();
                        getProcessingUnit().addStatisticsCalculation(id);
                        // TODO eli - optimize : remove existing pu statistics in case the zones has changed(due to replacePlannedZones)
                        maxNumberOfSamples = 
                                Math.max(
                                        maxNumberOfSamples,
                                        timeWindowStatistics.getMaxNumberOfSamples(
                                                config.getStatisticsPollingIntervalSeconds(), 
                                                TimeUnit.SECONDS));
                    }
                
            }
        }
        getLogger().info("Start statistics polling for " + getProcessingUnit().getName() + " to " + config.getStatisticsPollingIntervalSeconds() + " seconds, history size is " + maxNumberOfSamples + " samples.");
        getProcessingUnit().setStatisticsHistorySize(maxNumberOfSamples);

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
