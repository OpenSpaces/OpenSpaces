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

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfigurer;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingInstanceStatisticsException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingStatisticsException;

/**
 * @author itaif
 *
 */
public class AutoScalingSlaUtils {

    @SuppressWarnings("unchecked")
    public static int compare(Comparable<?> threshold, Object value) throws NumberFormatException {
        
        if (threshold.getClass().equals(value.getClass())) {
            return ((Comparable<Object>)threshold).compareTo(value);
        }

        return toDouble(threshold).compareTo(toDouble(value));       
    }

    private static Double toDouble(Object x) throws NumberFormatException{
        if (x instanceof Number) {
            return ((Number)x).doubleValue();
        }
        return Double.valueOf(x.toString());
    }
    
    /**
     * Validates that the specified statisticsId defined in the rule
     */
    public static Object getStatisticsValue(
            ProcessingUnit pu,
            Map<ProcessingUnitStatisticsId, Object> statistics,
            ProcessingUnitStatisticsId ruleStatisticsId) 
                    throws AutoScalingSlaEnforcementInProgressException {
        
        ruleStatisticsId.validate();
        for (final ProcessingUnitInstance instance : pu) {
            
            SingleInstanceStatisticsConfig singleInstanceStatistics = 
                new SingleInstanceStatisticsConfigurer()
                .instance(instance)
                .create();
            
            final ProcessingUnitStatisticsId singleInstanceLastSampleStatisticsId = 
                    ruleStatisticsId.shallowClone();
            singleInstanceLastSampleStatisticsId.setTimeWindowStatistics(new LastSampleTimeWindowStatisticsConfig());
            singleInstanceLastSampleStatisticsId.setInstancesStatistics(singleInstanceStatistics);
            singleInstanceLastSampleStatisticsId.validate();
            if (!statistics.containsKey(singleInstanceLastSampleStatisticsId)) {
                throw new AutoScalingInstanceStatisticsException(instance, singleInstanceLastSampleStatisticsId.getMetric());    
            }
            
            final ProcessingUnitStatisticsId singleInstanceStatisticsId =
                    ruleStatisticsId.shallowClone();
            singleInstanceStatisticsId.setTimeWindowStatistics(ruleStatisticsId.getTimeWindowStatistics());
            singleInstanceStatisticsId.setInstancesStatistics(singleInstanceStatistics);
            singleInstanceStatisticsId.validate();
            if (!statistics.containsKey(singleInstanceStatisticsId)) {
                throw new AutoScalingStatisticsException(pu, singleInstanceStatisticsId);
            }
        }
        
        Object value = statistics.get(ruleStatisticsId);
        if (value == null) {
            throw new AutoScalingStatisticsException(pu, ruleStatisticsId);
        }
        
        return value;
    }

}
