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
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsPerZonesConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaPolicy;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils;
import org.openspaces.grid.gsm.autoscaling.AutomaticCapacityCooldownValidator;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingConfigConflictException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingHighThresholdBreachedException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingLowThresholdBreachedException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.autoscaling.exceptions.PerZoneAutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaHasChangedException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.UndeployInProgressException;
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
    private CapacityRequirementsPerZones enforced;

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
        this.cooldownValidator.setCooldownAfterInstanceRemoved(config.getCooldownAfterScaleInSeconds(), TimeUnit.SECONDS);
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
                        DefaultElasticAutoScalingProgressChangedEvent.class);

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

        CapacityRequirements min = config.getMinCapacity().toCapacityRequirements();
        if (min == null) {
            throw new BeanConfigurationException("Minimum capacity requirements is undefined");
        }

        CapacityRequirements max = config.getMaxCapacity().toCapacityRequirements();
        if (max == null) {
            throw new BeanConfigurationException("Maximum capacity requirements is undefined");
        }

        if (min.greaterThan(max)) {
            throw new BeanConfigurationException("Maximum capacity (" + max
                    + ") is less than minimum capacity (" + min + ")");
        }

        CapacityRequirementsConfig initialConfig = config.getInitialCapacity();
        if (initialConfig != null) {
            CapacityRequirements initial = initialConfig.toCapacityRequirements();

            if (min.greaterThan(initial)) {
                throw new BeanConfigurationException("Initial capacity (" + initial
                        + ") is less than minimum capacity (" + min + ")");
            }

            if (initial.greaterThan(max)) {
                throw new BeanConfigurationException("Initial capacity (" + initial
                        + ") is greater than maximum capacity (" + min + ")");
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
    protected void recoverStateOnEsmStart() throws SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException, MachinesSlaEnforcementInProgressException, UndeployInProgressException {
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
        boolean planChanged = super.setPlannedCapacity(config);
        if (planChanged) {
            enablePuStatistics();
        }
        return planChanged;
    }

    @Override
    protected void enforceSla() throws SlaEnforcementInProgressException {

        PerZoneAutoScalingSlaEnforcementInProgressException pendingAutoscaleInProgressExceptions = new PerZoneAutoScalingSlaEnforcementInProgressException(getProcessingUnit(), "Multiple Exceptions");
        
        SlaEnforcementInProgressException pendingEnforcePlannedCapacityException = null;
        final CapacityRequirementsPerZones planned = super.getPlannedCapacity().toCapacityRequirementsPerZones();

        try {
            super.enforcePlannedCapacity();
            enforced = planned;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("enforcedCapacityRequirementsPerZones = " + planned);
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

            if (enforced == null) {
                // no prev capacityRequirements to work with
                throw e;
            }

            // no effect on instances yet... proceed with auto scaling rules
            // The reasoning is that it may take a long time for machines to start
            // and during that time the capacity requirements may need to change
            pendingEnforcePlannedCapacityException = e;
        }



        CapacityRequirementsPerZones newPlanned = new CapacityRequirementsPerZones();

        // make sure that we are not in the cooldown period
        // Notice this check is performed after PU is INTACT, meaning the USM is already started
        // @see DefaultAdmin#degradeUniversalServiceManagerProcessingUnitStatus()
        cooldownValidator.validate();
 
        Set<ZonesConfig> zoness = new HashSet<ZonesConfig>();
        
        if (isGridServiceAgentZonesAware()) {
            zoness.addAll(enforced.getZones());
        }
        else {
            zoness.add(getDefaultZones());
        }

        // enforce SLA for each zone separately.
        for (ZonesConfig zones : zoness) {

            try {
                enforceAutoScalingSla(zones, enforced, newPlanned);
            
            } catch (AutoScalingHighThresholdBreachedException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("High threshold breachned. Settings zones " + zones + " capacity to " + e.getNewCapacity());
                }
                newPlanned = newPlanned.set(zones, e.getNewCapacity());

            } catch (AutoScalingLowThresholdBreachedException e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Low threshold breachned. Settings zones " + zones + " capacity to " + e.getNewCapacity());
                }
                newPlanned = newPlanned.set(zones, e.getNewCapacity());

            } catch (AutoScalingSlaEnforcementInProgressException e) {
                // do not change the plan if an exception was raised
                final CapacityRequirements plannedForZones = planned.getZonesCapacityOrZero(zones);
                
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Copying exising zones " + zones +" capacity " + plannedForZones);
                }
                newPlanned = newPlanned.set(zones, plannedForZones);
                
                // exception in one zone should not influence running autoscaling in another zone.
                // save exceptions for later handling
                pendingAutoscaleInProgressExceptions.addReason(zones, e);
            }
        }

        // no need to call AbstractCapacityScaleStrategyBean#enforePlannedCapacity if no capacity change is needed.
        boolean planChanged = setPlannedCapacity(new CapacityRequirementsPerZonesConfig(newPlanned));

        if (pendingEnforcePlannedCapacityException != null) {
            // throw pending exception of previous manual scale capacity.
            // otherwise it could be lost when calling it again.
            throw pendingEnforcePlannedCapacityException;
        }
        
        if (pendingAutoscaleInProgressExceptions.hasReason()) {
            // exceptions during autoscaling sla enforcement per zone
            throw pendingAutoscaleInProgressExceptions;
        }
        
        if (planChanged) {
            // enforce new capacity requirements as soon as possible.
            // also exitting this method without an exception implies SLA is enforced
            super.enforcePlannedCapacity();
        }
    }
    
    private void enforceAutoScalingSla(ZonesConfig zones, CapacityRequirementsPerZones enforced, CapacityRequirementsPerZones newPlanned)
            throws AutoScalingSlaEnforcementInProgressException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing automatic scaling SLA.");
        }
        

        final AutoScalingSlaPolicy sla = new AutoScalingSlaPolicy();
        sla.setCapacityRequirements(enforced.getZonesCapacityOrZero(zones));

        CapacityRequirements minimum = config.getMinCapacity().toCapacityRequirements();
        CapacityRequirements maximum = config.getMaxCapacity().toCapacityRequirements();
        
        if (isGridServiceAgentZonesAware()) {
            CapacityRequirements maximumPerZone = config.getMaxCapacityPerZone().toCapacityRequirements();
            CapacityRequirements minimumPerZone = config.getMinCapacityPerZone().toCapacityRequirements();
            maximum = AutoScalingSlaUtils.getMaximumCapacity(maximum, maximumPerZone, enforced, newPlanned, zones);
            minimum = AutoScalingSlaUtils.getMinimumCapacity(minimum, minimumPerZone, enforced, newPlanned, zones);            
        } 
        
        // for now we assume these values are already given as per zone.
        sla.setMaxCapacity(maximum);
        sla.setMinCapacity(minimum);
        sla.setRules(config.getRules());
        sla.setZonesConfig(zones);
        sla.setContainerMemoryCapacityInMB(getGridServiceContainerConfig().getMaximumMemoryCapacityInMB());
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Automatic Scaling SLA Policy: " + sla);
        }

        try {
            
            if (!maximum.greaterOrEquals(minimum)) {
                throw new AutoScalingConfigConflictException(getProcessingUnit(), minimum, maximum, 
                        zones.getZones(), enforced, newPlanned);
            }
            
            autoScalingEndpoint.enforceSla(sla);
            autoScalingCompletedEvent();
        }
        catch (AutoScalingSlaEnforcementInProgressException e) {
            autoScalingInProgressEvent(e);
            throw e;
        }
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
                    .monitor(statisticsId.getMonitor())
                    .timeWindowStatistics(statisticsId.getTimeWindowStatistics())
                    .create();
                
                maxNumberOfSamples = 
                        Math.max(
                                maxNumberOfSamples,
                                timeWindowStatistics.getMaxNumberOfSamples(
                                        config.getStatisticsPollingIntervalSeconds(), 
                                        TimeUnit.SECONDS));
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("adding statistics calculation : " + id);                        
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
                            .monitor(statisticsId.getMonitor())
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
