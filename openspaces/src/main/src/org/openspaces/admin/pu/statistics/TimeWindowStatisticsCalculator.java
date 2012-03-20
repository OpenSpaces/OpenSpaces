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
package org.openspaces.admin.pu.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;

import edu.emory.mathcs.backport.java.util.Collections;

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
        

        Map<ProcessingUnitStatisticsId, Set<ProcessingUnitStatisticsId>> statisticsIdsGroupedByErasedStatisticsId = eraseTimeWindowStatistics(newStatisticsIds);
        Set<ProcessingUnitStatisticsId> erasedStatisticsIds = statisticsIdsGroupedByErasedStatisticsId.keySet();
        // TODO: Expand EachSingleInstanceStatisticsConfig in newStatisticsIds 
        // based on SingleInstanceStatisticsConfig in processingUnitStatistics
        Map<ProcessingUnitStatisticsId, List<Object>> timeLine = getValues(processingUnitStatistics, erasedStatisticsIds);
        
        for (Map.Entry<ProcessingUnitStatisticsId, List<Object>> pair : timeLine.entrySet()) {
            
            ProcessingUnitStatisticsId erasedStatisticsId = pair.getKey();
            List<Object> sortedValues = pair.getValue();
            try {
                Collections.sort(sortedValues);
            }
            catch (ClassCastException e) {
                logger.info(erasedStatisticsId.getMetric() + " class type is not Comparable",e);
                continue;
            }
            
            Set<ProcessingUnitStatisticsId> statisticsIds = statisticsIdsGroupedByErasedStatisticsId.get(erasedStatisticsId);
            for (ProcessingUnitStatisticsId statisticsId : statisticsIds) {
                TimeWindowStatisticsConfig timeWindowStatistics = statisticsId.getTimeWindowStatistics();
                Object value = applyTimeWindowStatisticsOnSoretedList(timeWindowStatistics,sortedValues);
                processingUnitStatistics.addStatistics(statisticsId, value);    
            }
        }
    }

    /**
     * Groups statisticsIds by replacing their TimeWindowStatistics with {@link LastSampleTimeWindowStatisticsConfig}
     */
    private Map<ProcessingUnitStatisticsId,ProcessingUnitStatisticsId> eraseTimeWindowStatistics(Set<ProcessingUnitStatisticsId> statisticsIds) {
        Map<ProcessingUnitStatisticsId,ProcessingUnitStatisticsId> groupBy = new HashMap<ProcessingUnitStatisticsId,ProcessingUnitStatisticsId>();
        for (ProcessingUnitStatisticsId statisticsId : statisticsIds) {
            statisticsId.validate();
            TimeWindowStatisticsConfig timeWindowStatistics = statisticsId.getTimeWindowStatistics();
            InstancesStatisticsConfig instancesStatistics = statisticsId.getInstancesStatistics();
            if (!(instancesStatistics instanceof SingleInstanceStatisticsConfig) {)
                throw new IllegalArgumentException("Unsupported statisticsId. Only "+SingleInstanceStatisticsConfig.class.getName() +" is supported. Offending id="+statisticsId);
            }
            // we need to remove all traces of the time window function in order to aggregate values only by time window
            // LastStatistics is used just as a non-null constant that can hash time window configuration
            TimeWindowStatisticsConfig erasedTimeWindow = new LastSampleTimeWindowStatisticsConfig();
            erasedTimeWindow.setMaximumTimeWindowSeconds(timeWindowStatistics.getMaximumTimeWindowSeconds());
            erasedTimeWindow.setMinimumTimeWindowSeconds(timeWindowStatistics.getMinimumTimeWindowSeconds());
            erasedTimeWindow.setTimeWindowSeconds(timeWindowStatistics.getTimeWindowSeconds());
            
            ProcessingUnitStatisticsId key = 
                    new ProcessingUnitStatisticsIdConfigurer()
                    .metric(statisticsId.getMetric())
                    .monitor(statisticsId.getMonitor())
                    .instancesStatistics(statisticsId.getInstancesStatistics())
                    .timeWindowStatistics(erasedTimeWindow)
                    .create();
            
            groupBy.put(key, statisticsId);
        }
        return groupBy;
    }

    /**
     * @param value
     * @return
     */
    private Object calculate(List<Object> value) {
        // TODO Auto-generated method stub
        return null;
    }

    private Map<ProcessingUnitStatisticsId, List<Object>> getValues(
            final InternalProcessingUnitStatistics processingUnitStatistics,
            final Set<ProcessingUnitStatisticsId> newStatisticsIds) {
        
        ProcessingUnitStatistics statistics = processingUnitStatistics;
        
        final Map<ProcessingUnitStatisticsId, List<Object>> valuesTimeline = new HashMap<ProcessingUnitStatisticsId, List<Object>>();
        final Map<ProcessingUnitStatisticsId, List<Object>> finalValuesTimeline = new HashMap<ProcessingUnitStatisticsId, List<Object>>();
        long timeDelta = 0;
        while (statistics != null) {
            long prevTimeDelta = timeDelta;
            timeDelta = processingUnitStatistics.getAdminTimestamp() - statistics.getAdminTimestamp();
            
            final Map<ProcessingUnitStatisticsId, Object> values = statistics.getStatistics();
            for (final ProcessingUnitStatisticsId statisticsId : newStatisticsIds) {
                
                final long timeWindowSeconds = statisticsId.getTimeWindowStatistics().getTimeWindowSeconds();
                final long minTimeWindowSeconds = statisticsId.getTimeWindowStatistics().getMinimumTimeWindowSeconds();
                final long maxTimeWindowSeconds = statisticsId.getTimeWindowStatistics().getMaximumTimeWindowSeconds();
                
                if (timeDelta == 0) {
                    valuesTimeline.put(statisticsId, new ArrayList<Object>());
                }
                
                final List<Object> timeline = valuesTimeline.get(statisticsId);
                
                if (timeline != null) {
                    
                    final Object value = getValue(values, statisticsId);
                    if (timeDelta > maxTimeWindowSeconds || value == null) {
                        
                        // sample would cause too much gap or does not exist.
                        valuesTimeline.remove(statisticsId);
                        
                        if (prevTimeDelta > minTimeWindowSeconds) {
                            finalValuesTimeline.put(statisticsId, timeline);
                        }
                        continue;
                    }
                    
                    timeline.add(value);
                    
                    if (timeDelta > timeWindowSeconds) {
                        // collected enough samples
                        valuesTimeline.remove(statisticsId);
                        
                        finalValuesTimeline.put(statisticsId, timeline);
                        continue;
                    }
                }
            }
            statistics = statistics.getPrevious();
        }
        return finalValuesTimeline;
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
