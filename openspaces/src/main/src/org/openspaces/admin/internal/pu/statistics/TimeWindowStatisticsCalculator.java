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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.pu.statistics.AbstractTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.AverageTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.MaximumTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.MinimumTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.PercentileTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;

/**
 * @author itaif
 *
 */
public class TimeWindowStatisticsCalculator implements InternalProcessingUnitStatisticsCalculator {

    private final Log logger = LogFactory.getLog(this.getClass());
    
    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator#calculateNewStatistics(org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics, java.util.List)
     */
    @Override
    public void calculateNewStatistics(
            final InternalProcessingUnitStatistics processingUnitStatistics,
            final Set<ProcessingUnitStatisticsId> newStatisticsIds) {
        
        Map<ProcessingUnitStatisticsId, Set<TimeWindowStatisticsConfig>> statisticsIdsPerErasedStatisticsId = eraseTimeWindowStatistics(newStatisticsIds);
        Set<ProcessingUnitStatisticsId> erasedStatisticsIds = statisticsIdsPerErasedStatisticsId.keySet();
        Map<ProcessingUnitStatisticsId, StatisticsObjectList> timeLine = getValues(processingUnitStatistics, erasedStatisticsIds);
        
        for (Map.Entry<ProcessingUnitStatisticsId, StatisticsObjectList> pair : timeLine.entrySet()) {
            
            ProcessingUnitStatisticsId erasedStatisticsId = pair.getKey();
            StatisticsObjectList values = pair.getValue();
            
            for (TimeWindowStatisticsConfig timeWindowStatistics : statisticsIdsPerErasedStatisticsId.get(erasedStatisticsId)) {
                
                ProcessingUnitStatisticsId statisticsId = unerase(erasedStatisticsId,timeWindowStatistics);
            
                Object value = null;
        
                // TODO: Consider visitor pattern to decouple the conversion of statistics config to
                // a value from both TimeWindowStatisticsCalculator and the various Config objects
                if (timeWindowStatistics instanceof AverageTimeWindowStatisticsConfig) {
                    value = values.getAverage();
                }
                else if (timeWindowStatistics instanceof MinimumTimeWindowStatisticsConfig) {
                    value = values.getMinimum();
                }
                else if (timeWindowStatistics instanceof MaximumTimeWindowStatisticsConfig) {
                    value = values.getMaximum();
                }
                else if (timeWindowStatistics instanceof PercentileTimeWindowStatisticsConfig) {
                    double precentile =((PercentileTimeWindowStatisticsConfig) timeWindowStatistics).getPercentile();
                    value = values.getPercentile(precentile);
                }
                
                if (value != null) {
                    processingUnitStatistics.addStatistics(statisticsId, value);    
                }
                else {
                    if (logger.isWarnEnabled()) {
                        logger.warn(timeWindowStatistics.getClass() + " is not supported");
                    }   
                }
                
            }
        }
        
    }

    /**
     * Groups statisticsIds by replacing their TimeWindowStatistics with {@link ErasedTimeWindowStatisticsConfig}
     */
    private Map<ProcessingUnitStatisticsId,Set<TimeWindowStatisticsConfig>> eraseTimeWindowStatistics(Set<ProcessingUnitStatisticsId> statisticsIds) {

        Map<ProcessingUnitStatisticsId, Set<TimeWindowStatisticsConfig>> groupBy = new HashMap<ProcessingUnitStatisticsId, Set<TimeWindowStatisticsConfig>>();
        for (ProcessingUnitStatisticsId statisticsId : statisticsIds) {

            ProcessingUnitStatisticsId key = erase(statisticsId);

            if (!groupBy.containsKey(key)) {
                groupBy.put(key, new HashSet<TimeWindowStatisticsConfig>());
            }

            groupBy.get(key).add(statisticsId.getTimeWindowStatistics());

        }
        return groupBy;
    }

    /**
     * @param key
     */
    private ProcessingUnitStatisticsId erase(ProcessingUnitStatisticsId statisticsId) {
        
        statisticsId.validate();
        
        if (!(statisticsId.getInstancesStatistics() instanceof SingleInstanceStatisticsConfig)) {
            throw new IllegalArgumentException("Unsupported statisticsId. Only "+SingleInstanceStatisticsConfig.class.getName() +" is supported. Offending id="+statisticsId);
        }
        
        if (!(statisticsId.getTimeWindowStatistics() instanceof AbstractTimeWindowStatisticsConfig)) {
            return statisticsId;
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
     * @param erasedStatisticsId
     * @param timeWindowStatistics2
     * @return
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
                        timeline.add(value);
                        
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
