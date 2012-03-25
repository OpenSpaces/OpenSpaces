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
package org.openspaces.admin.internal.pu.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openspaces.admin.pu.statistics.EachSingleInstanceStatisticsConfig;
import org.openspaces.admin.pu.statistics.InstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;

/**
 * Aggregates samples from different instances using different functions defined by {@link InstancesStatisticsConfig}
 * that is stored in {@link ProcessingUnitStatisticsId}s
 * @author itaif
 * @since 9.0.0
 *
 */
public class InstancesStatisticsCalculator implements InternalProcessingUnitStatisticsCalculator {

    @Override
    public void calculateNewStatistics(
            InternalProcessingUnitStatistics processingUnitStatistics,
            Iterable<ProcessingUnitStatisticsId> statisticsIds) {
        
        Map<ProcessingUnitStatisticsId, Set<InstancesStatisticsConfig>> instancesStatisticsPerErasedStatisticsId = eraseInstancesStatistics(statisticsIds);
        Set<ProcessingUnitStatisticsId> erasedStatisticsIds = instancesStatisticsPerErasedStatisticsId.keySet();
        Map<ProcessingUnitStatisticsId, StatisticsObjectList> valuesPerErasedStatisticsId = getValues(processingUnitStatistics, erasedStatisticsIds);
        
        for (Map.Entry<ProcessingUnitStatisticsId, StatisticsObjectList> pair : valuesPerErasedStatisticsId.entrySet()) {
            
            ProcessingUnitStatisticsId erasedStatisticsId = pair.getKey();
            StatisticsObjectList values = pair.getValue();
            
            for (InstancesStatisticsConfig instancesStatistics : instancesStatisticsPerErasedStatisticsId.get(erasedStatisticsId)) {
                if (instancesStatistics instanceof StatisticsObjectListFunction) {
                    StatisticsObjectListFunction statisticsFunc = (StatisticsObjectListFunction) instancesStatistics;
                    Object value = statisticsFunc.calc(values);
                    ProcessingUnitStatisticsId statisticsId = unerase(erasedStatisticsId,instancesStatistics);
                    processingUnitStatistics.addStatistics(statisticsId, value);    
                }
            }
        }
    }

    /**
     * @param processingUnitStatistics
     * @param erasedStatisticsIds
     * @return
     */
    private Map<ProcessingUnitStatisticsId, StatisticsObjectList> getValues(
            InternalProcessingUnitStatistics processingUnitStatistics,
            Set<ProcessingUnitStatisticsId> erasedStatisticsIds) {
        
        Map<ProcessingUnitStatisticsId, StatisticsObjectList> values = new HashMap<ProcessingUnitStatisticsId, StatisticsObjectList>();
        for (Entry<ProcessingUnitStatisticsId, Object>  pair : processingUnitStatistics.getStatistics().entrySet()) {
            ProcessingUnitStatisticsId statisticsId = pair.getKey();
            if (statisticsId.getInstancesStatistics() instanceof EachSingleInstanceStatisticsConfig) {
                throw new IllegalArgumentException("Unsupported statisticsId. Use " + SingleInstanceStatisticsConfig.class + " instead of " + EachSingleInstanceStatisticsConfig.class);
            }
            if (statisticsId.getInstancesStatistics() instanceof SingleInstanceStatisticsConfig) {
                ProcessingUnitStatisticsId erasedStatisticsId = erase(statisticsId);
                if (erasedStatisticsIds.contains(erasedStatisticsId)) {
                    if (!values.containsKey(erasedStatisticsId)) {
                        values.put(erasedStatisticsId, new StatisticsObjectList());
                    }
                    Object value = pair.getValue();
                    values.get(erasedStatisticsId).add(value);
                }
            }
        }
        return values;
    }

    /**
     * Groups statisticsIds by replacing their InstancesStatistics with {@link ErasedInstancesStatisticsConfig}
     */
    private Map<ProcessingUnitStatisticsId,Set<InstancesStatisticsConfig>> eraseInstancesStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds) {

        Map<ProcessingUnitStatisticsId, Set<InstancesStatisticsConfig>> groupBy = new HashMap<ProcessingUnitStatisticsId, Set<InstancesStatisticsConfig>>();
        for (ProcessingUnitStatisticsId statisticsId : statisticsIds) {

            InstancesStatisticsConfig instancesStatistics = statisticsId.getInstancesStatistics();
            ProcessingUnitStatisticsId key = erase(statisticsId);

            if (!groupBy.containsKey(key)) {
                groupBy.put(key, new HashSet<InstancesStatisticsConfig>());
            }
            
            groupBy.get(key).add(instancesStatistics);
        }
        return groupBy;
    }
    

    /**
     * Erases the InstancesStatistics from the specified statisticsId
     */
    private ProcessingUnitStatisticsId erase(ProcessingUnitStatisticsId statisticsId) {
        
        statisticsId.validate();
                
        ProcessingUnitStatisticsId erased = clone(statisticsId);
        // erase statistics function from hashmap key
        erased.setInstancesStatistics(
                new ErasedInstancesStatisticsConfig());
        return erased;
    }
    
    private ProcessingUnitStatisticsId clone(ProcessingUnitStatisticsId statisticsId) {
        return  new ProcessingUnitStatisticsIdConfigurer()
                .metric(statisticsId.getMetric())
                .monitor(statisticsId.getMonitor())
                .instancesStatistics(statisticsId.getInstancesStatistics())
                .timeWindowStatistics(statisticsId.getTimeWindowStatistics())
                .create();
    }


    /**
     * restores the specified instancesStatistics to the statisticsId
     */
    private ProcessingUnitStatisticsId unerase(
            ProcessingUnitStatisticsId erasedStatisticsId,
            InstancesStatisticsConfig instancesStatistics) {
        
        if (!(erasedStatisticsId.getInstancesStatistics() instanceof ErasedInstancesStatisticsConfig)) {
            return erasedStatisticsId;
        }
        
        ProcessingUnitStatisticsId unerased = clone(erasedStatisticsId);
        unerased.setInstancesStatistics(instancesStatistics);
        return unerased;
    }
}
