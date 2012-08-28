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

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingInstanceStatisticsException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingStatisticsException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;

/**
 * @author itaif
 * 
 */
public class AutoScalingSlaUtils {

    private static Log logger = LogFactory.getLog(AutoScalingSlaUtils.class.getName());

    @SuppressWarnings("unchecked")
    public static int compare(Comparable<?> threshold, Object value) throws NumberFormatException {

        if (threshold.getClass().equals(value.getClass())) {
            return ((Comparable<Object>) threshold).compareTo(value);
        }

        return toDouble(threshold).compareTo(toDouble(value));
    }

    private static Double toDouble(Object x) throws NumberFormatException {
        if (x instanceof Number) {
            return ((Number) x).doubleValue();
        }
        return Double.valueOf(x.toString());
    }

    /**
     * Calculates the maximum capacity for the specified zones.
     * 
     * The algorithm tries to be very conservative and may return a smaller value than maxPerZones.
     * otherCapacity = sum (foreach otherZones!=zones --> min(maxPerZone,max(last[otherZones],newPlanned[otherZones]))
     * return  min(maxPerZones, totalMax - otherCapacity)
     *  
     * @param totalMax - the maximum capacity when adding up all zones 
     * @param maxPerZone - the maximum capacity per zone
     * @param last - the last enforced (allocated) capacity per zone
     * @param newPlanned - the new (planned) capacity per zone (could be zero if no plan yet)
     * @param zones - the zone for which the maximum capacity is requested 
     * @param zoness - the complete list of zones
     */
    public static CapacityRequirements getMaximumCapacity(CapacityRequirements totalMax,
            CapacityRequirements maxPerZone, CapacityRequirementsPerZones last,
            CapacityRequirementsPerZones newPlanned, ZonesConfig zones, Set<ZonesConfig> zoness) {

        CapacityRequirements maximumCapacity = totalMax; // initial

        for (ZonesConfig otherZone : zoness) {
            if (!zones.equals(otherZone)) {
                CapacityRequirements otherLastEnforced = last.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherNewPlanned = newPlanned.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherMaximumCapacity = otherLastEnforced.max(otherNewPlanned).min(maxPerZone);
                maximumCapacity = maximumCapacity.subtractOrZero(otherMaximumCapacity);
            }
        }
        maximumCapacity = maximumCapacity.min(maxPerZone);
        return maximumCapacity;
    }

    /**
     * Calculates the minimum capacity for the specified zones.
     * 
     * The algorithm tries to be very conservative and may return a bigger value than minPerZones.
     * 
     * otherCapacity = sum (foreach otherZones!=zones --> max(minPerZone,min(last[otherZones], newPlanned[otherZones]))
     * return  min(maxPerZones, totalMin - otherCapacity)
     *  
     * @param totalMin - the minimum capacity when adding up all zones 
     * @param minPerZone - the minimum capacity per zone
     * @param last - the last enforced (allocated) capacity per zone
     * @param newPlanned - the new (planned) capacity per zone (could be zero if no plan yet, in that case it is ignored)
     * @param zones - the zone for which the maximum capacity is requested 
     * @param zoness - the complete list of zones
     */
    public static CapacityRequirements getMinimumCapacity(CapacityRequirements totalMin,
            CapacityRequirements minPerZone, CapacityRequirementsPerZones last,
            CapacityRequirementsPerZones newPlanned, ZonesConfig zones, Set<ZonesConfig> zoness) {

        CapacityRequirements minimumRequierements = totalMin; // initial

        for (ZonesConfig otherZone : zoness) {
            if (!zones.equals(otherZone)) {
                CapacityRequirements otherLastEnforced = last.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherNewPlanned = newPlanned.getZonesCapacityOrZero(otherZone);
                CapacityRequirements otherMinimumCapacity = null;
                if (otherNewPlanned.equalsZero()) {
                    // autoscaling did not calculate new capacity for 'otherZone' yet.
                    otherMinimumCapacity = otherLastEnforced.max(minPerZone);
                } else {
                    otherMinimumCapacity = otherLastEnforced.min(otherNewPlanned).max(minPerZone);
                }
                minimumRequierements = minimumRequierements.subtractOrZero(otherMinimumCapacity);
            }
        }
        // enforce minimum capacity per zone to be at least the pre-defince minimum capacity per
        // zone
        minimumRequierements = minimumRequierements.max(minPerZone);
        return minimumRequierements;
    }

    /**
     * Validates that the specified statisticsId defined in the rule
     */
    public static Object getStatisticsValue(ProcessingUnit pu, Map<ProcessingUnitStatisticsId, Object> statistics,
            ProcessingUnitStatisticsId ruleStatisticsId) throws AutoScalingSlaEnforcementInProgressException {

        ruleStatisticsId.validate();
        for (final ProcessingUnitInstance instance : pu) {

            ExactZonesConfig puInstanceExactZones = ((InternalProcessingUnit) pu).getHostingGridServiceAgentZones(instance);
            if (puInstanceExactZones.isStasfies(ruleStatisticsId.getAgentZones())) {

                SingleInstanceStatisticsConfig singleInstanceStatistics = new SingleInstanceStatisticsConfigurer().instance(
                        instance)
                    .create();

                final ProcessingUnitStatisticsId singleInstanceLastSampleStatisticsId = ruleStatisticsId.shallowClone();
                singleInstanceLastSampleStatisticsId.setTimeWindowStatistics(new LastSampleTimeWindowStatisticsConfig());
                singleInstanceLastSampleStatisticsId.setInstancesStatistics(singleInstanceStatistics);
                singleInstanceLastSampleStatisticsId.validate();

                if (!statistics.containsKey(singleInstanceLastSampleStatisticsId)) {
                    AutoScalingInstanceStatisticsException exception = new AutoScalingInstanceStatisticsException(
                            instance, singleInstanceLastSampleStatisticsId.getMetric());
                    if (logger.isTraceEnabled()) {
                        logger.trace("Failed to find statistics id = " + singleInstanceLastSampleStatisticsId
                                + " in pu statistics. current statistics key set = " + statistics.keySet(), exception);
                    }
                    throw exception;
                }

                final ProcessingUnitStatisticsId singleInstanceStatisticsId = ruleStatisticsId.shallowClone();
                singleInstanceStatisticsId.setTimeWindowStatistics(ruleStatisticsId.getTimeWindowStatistics());
                singleInstanceStatisticsId.setInstancesStatistics(singleInstanceStatistics);
                singleInstanceStatisticsId.validate();

                if (!statistics.containsKey(singleInstanceStatisticsId)) {
                    AutoScalingStatisticsException exception = new AutoScalingInstanceStatisticsException(instance,
                            singleInstanceStatisticsId.getMetric());
                    if (logger.isTraceEnabled()) {
                        logger.trace("Failed to find statistics id = " + singleInstanceStatisticsId
                                + " in pu statistics. current statistics key set = " + statistics.keySet(), exception);
                    }
                    throw exception;
                }

                final ProcessingUnitStatisticsId singleInstanceTimeWindowZoneStatisticsId = ruleStatisticsId.shallowClone();
                singleInstanceTimeWindowZoneStatisticsId.setTimeWindowStatistics(ruleStatisticsId.getTimeWindowStatistics());
                singleInstanceTimeWindowZoneStatisticsId.setAgentZones(ruleStatisticsId.getAgentZones());
                singleInstanceTimeWindowZoneStatisticsId.setInstancesStatistics(singleInstanceStatistics);
                singleInstanceTimeWindowZoneStatisticsId.validate();

                if (!statistics.containsKey(singleInstanceTimeWindowZoneStatisticsId)) {
                    AutoScalingStatisticsException exception = new AutoScalingInstanceStatisticsException(instance,
                            singleInstanceTimeWindowZoneStatisticsId.getMetric());
                    if (logger.isTraceEnabled()) {
                        logger.trace("Failed to find statistics id = " + singleInstanceTimeWindowZoneStatisticsId
                                + " in pu statistics. current statistics key set = " + statistics.keySet(), exception);
                    }
                    throw exception;
                }
            }
        }

        Object value = statistics.get(ruleStatisticsId);
        if (value == null) {
            logger.debug("statistics value for " + ruleStatisticsId + " was null.");
            throw new AutoScalingStatisticsException(pu, ruleStatisticsId);
        }

        return value;
    }

}
