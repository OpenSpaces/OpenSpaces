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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.pu.statistics.AbstractTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;

/**
 * Aggregates samples from a certain time window using different functions defined by {@link TimeWindowStatisticsConfig}
 * that is stored in {@link ProcessingUnitStatisticsId}s
 * @author itaif
 * @since 9.0.0
 */
public class TimeWindowStatisticsCalculator implements InternalProcessingUnitStatisticsCalculator {
    
    private final Log logger = LogFactory.getLog(this.getClass());
    
    @Override
    public void calculateNewStatistics(
            final InternalProcessingUnitStatistics processingUnitStatistics,
            final Collection<ProcessingUnitStatisticsId> statisticsIds) {
        
        if (logger.isTraceEnabled()) {
            logger.trace("calculateNewStatistics(processingUnitStatistics="+processingUnitStatistics+" , statisticsIds="+ statisticsIds);
        }
        Map<ProcessingUnitStatisticsId, Set<TimeWindowStatisticsConfig>> timeWindowStatisticsPerErasedStatisticsId = eraseTimeWindowStatistics(statisticsIds);
        Set<ProcessingUnitStatisticsId> erasedStatisticsIds = timeWindowStatisticsPerErasedStatisticsId.keySet();
        Map<ProcessingUnitStatisticsId, StatisticsObjectList> valuesPerErasedStatisticsId = getValues(processingUnitStatistics, erasedStatisticsIds);
        
        for (Map.Entry<ProcessingUnitStatisticsId, StatisticsObjectList> pair : valuesPerErasedStatisticsId.entrySet()) {
            
            ProcessingUnitStatisticsId erasedStatisticsId = pair.getKey();
            StatisticsObjectList values = pair.getValue();
            
            for (TimeWindowStatisticsConfig timeWindowStatistics : timeWindowStatisticsPerErasedStatisticsId.get(erasedStatisticsId)) {
                
                if (timeWindowStatistics instanceof StatisticsObjectListFunction) {
                    StatisticsObjectListFunction statisticsFunc = (StatisticsObjectListFunction) timeWindowStatistics;
                    Object value = statisticsFunc.calc(values);
                    ProcessingUnitStatisticsId statisticsId = unerase(erasedStatisticsId,timeWindowStatistics);
                    processingUnitStatistics.addStatistics(statisticsId, value);    
                }
            }
        }
        
    }

    /**
     * Groups statisticsIds by replacing their TimeWindowStatistics with {@link ErasedTimeWindowStatisticsConfig}
     */
    private Map<ProcessingUnitStatisticsId,Set<TimeWindowStatisticsConfig>> eraseTimeWindowStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds) {

        Map<ProcessingUnitStatisticsId, Set<TimeWindowStatisticsConfig>> groupBy = new HashMap<ProcessingUnitStatisticsId, Set<TimeWindowStatisticsConfig>>();
        for (ProcessingUnitStatisticsId statisticsId : statisticsIds) {

            if (statisticsId.getTimeWindowStatistics() instanceof AbstractTimeWindowStatisticsConfig) {
                ProcessingUnitStatisticsId key = erase(statisticsId);
    
                if (!groupBy.containsKey(key)) {
                    groupBy.put(key, new HashSet<TimeWindowStatisticsConfig>());
                }
    
                groupBy.get(key).add(statisticsId.getTimeWindowStatistics());
            }
        }
        return groupBy;
    }

    /**
     * Erases the TimeWindowStatistics from the specified statisticsId
     */
    private ProcessingUnitStatisticsId erase(ProcessingUnitStatisticsId statisticsId) {
        
        statisticsId.validate();
        
        if (!(statisticsId.getInstancesStatistics() instanceof SingleInstanceStatisticsConfig)) {
            throw new IllegalArgumentException("Unsupported statisticsId. Only "+SingleInstanceStatisticsConfig.class.getName() +" is supported. Offending id="+statisticsId);
        }
                
        ProcessingUnitStatisticsId erased = clone(statisticsId);
        // erase statistics function from hashmap key
        erased.setTimeWindowStatistics(
                new ErasedTimeWindowStatisticsConfig((AbstractTimeWindowStatisticsConfig)erased.getTimeWindowStatistics()));
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
     * restores the specified timeWindowStatistics to the statisticsId
     */
    private ProcessingUnitStatisticsId unerase(
            ProcessingUnitStatisticsId erasedStatisticsId,
            TimeWindowStatisticsConfig timeWindowStatistics) {
        
        if (!(erasedStatisticsId.getTimeWindowStatistics() instanceof ErasedTimeWindowStatisticsConfig)) {
            return erasedStatisticsId;
        }
        
        ProcessingUnitStatisticsId unerased = clone(erasedStatisticsId);
        unerased.setTimeWindowStatistics(timeWindowStatistics);
        return unerased;
    }

    private Map<ProcessingUnitStatisticsId, StatisticsObjectList> getValues(
            final InternalProcessingUnitStatistics processingUnitStatistics,
            final Set<ProcessingUnitStatisticsId> newStatisticsIds) {
        
        if (processingUnitStatistics == null) {
            throw new IllegalArgumentException("processingUnitStatistics cannot be null");
        }
        
        ProcessingUnitStatistics statistics = processingUnitStatistics;
        
        final Map<ProcessingUnitStatisticsId, StatisticsObjectList> temporaryValues = new HashMap<ProcessingUnitStatisticsId, StatisticsObjectList>();
        for (final ProcessingUnitStatisticsId statisticsId : newStatisticsIds) {
            // initialize
            temporaryValues.put(statisticsId, new StatisticsObjectList());
        }
        
        final Map<ProcessingUnitStatisticsId, StatisticsObjectList> returnValues = new HashMap<ProcessingUnitStatisticsId, StatisticsObjectList>();
        
        long timeDelta = 0;
        while (statistics != null) {
            
            timeDelta = processingUnitStatistics.getAdminTimestamp() - statistics.getAdminTimestamp();
            
            final Map<ProcessingUnitStatisticsId, Object> values = statistics.getStatistics();
            for (final ProcessingUnitStatisticsId statisticsId : newStatisticsIds) {
                
                final StatisticsObjectList timeline = temporaryValues.get(statisticsId);
                
                ErasedTimeWindowStatisticsConfig timeWindowStatistics = (ErasedTimeWindowStatisticsConfig) statisticsId.getTimeWindowStatistics();
                timeWindowStatistics.validate();
                final long timeWindowMilliSeconds = TimeUnit.SECONDS.toMillis(timeWindowStatistics.getTimeWindowSeconds());
                final long minTimeWindowMilliSeconds = TimeUnit.SECONDS.toMillis(timeWindowStatistics.getMinimumTimeWindowSeconds());
                final long maxTimeWindowMilliSeconds = TimeUnit.SECONDS.toMillis(timeWindowStatistics.getMaximumTimeWindowSeconds());
                                
                if (timeline != null) {
                    
                    final Object value = getValue(values, statisticsId);
                    if (timeDelta > maxTimeWindowMilliSeconds || value == null) {
                        
                        // invalid sample. Don't collect any more values.
                        temporaryValues.remove(statisticsId);
                    }
                    else {
                        //valid sample
                        long timeStamp = statistics.getAdminTimestamp();
                        timeline.add(value, timeStamp);
                        
                        if (timeDelta >= minTimeWindowMilliSeconds) {
                            // valid return value
                            returnValues.put(statisticsId, timeline);
                        }
                        
                        if (timeDelta >= timeWindowMilliSeconds) {
                            // Don't collect any more values.
                            temporaryValues.remove(statisticsId);
                        }
                    }
                }
            }
            statistics = statistics.getPrevious();
        }
        return returnValues;
    }

    private Object getValue(
            final Map<ProcessingUnitStatisticsId, Object> values,
            final ProcessingUnitStatisticsId statisticsId) {
        
        return values.get(
                new ProcessingUnitStatisticsIdConfigurer()
                .metric(statisticsId.getMetric())
                .monitor(statisticsId.getMonitor())
                .instancesStatistics(statisticsId.getInstancesStatistics())
                .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
                .create()
        );
    }

}
