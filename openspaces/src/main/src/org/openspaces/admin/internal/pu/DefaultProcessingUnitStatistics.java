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
package org.openspaces.admin.internal.pu;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openspaces.admin.internal.pu.statistics.DefaultProcessingUnitStatisticsCalculatorFactory;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculatorClassProvider;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

public class DefaultProcessingUnitStatistics implements InternalProcessingUnitStatistics {

    private volatile ProcessingUnitStatistics previous;

    private final long adminTimestamp;
    
    private final Map<ProcessingUnitStatisticsId, Object> statistics;

    private final DefaultProcessingUnitStatisticsCalculatorFactory statisticsCalculatorFactory;
    
    public DefaultProcessingUnitStatistics(
            long adminTimestamp, 
            ProcessingUnitStatistics lastStatistics,
            int historySize,
            DefaultProcessingUnitStatisticsCalculatorFactory statisticsCalculatorFactory) {
    
        this.statisticsCalculatorFactory = statisticsCalculatorFactory;
        this.statistics = new HashMap<ProcessingUnitStatisticsId, Object>();
        this.adminTimestamp = adminTimestamp;
        this.previous = lastStatistics;
       
        if (lastStatistics != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStatistics.getPrevious() == null) {
                    break;
                }
                lastStatistics = lastStatistics.getPrevious();
            }
            ((DefaultProcessingUnitStatistics)lastStatistics).previous = null;
        }
        
    }
    
    @Override
    public long getAdminTimestamp() {
        return this.adminTimestamp;
    }
    
    @Override
    public ProcessingUnitStatistics getPrevious() {
        return this.previous;
    }

    @Override
    public Map<ProcessingUnitStatisticsId, Object> getStatistics() {
        return Collections.unmodifiableMap(statistics);
    }

    @Override
    public void calculateStatistics(ProcessingUnitStatisticsId[] statisticsIds) {
        
        // first aggregate each instances statistics individually as a function of time
        calculateStatistics(toIdsPerTimeWindowCalculator(statisticsIds));
        
        // then aggregate all instances statistics (measured and calculated) into a single cluster value
        calculateStatistics(toIdsPerInstancesCalculator(statisticsIds));
    }

    private Map<InternalProcessingUnitStatisticsCalculatorClassProvider, Set<ProcessingUnitStatisticsId>> toIdsPerTimeWindowCalculator(
            ProcessingUnitStatisticsId[] statisticsIds) {
        final Map<InternalProcessingUnitStatisticsCalculatorClassProvider,Set<ProcessingUnitStatisticsId>> idsPerCalculatorFactory = new HashMap<InternalProcessingUnitStatisticsCalculatorClassProvider,Set<ProcessingUnitStatisticsId>>();
        
        for (final ProcessingUnitStatisticsId statisticsId : statisticsIds) {
            
            final InternalProcessingUnitStatisticsCalculatorClassProvider calculatorFactory = 
                    (InternalProcessingUnitStatisticsCalculatorClassProvider) statisticsId.getTimeWindowStatistics();
            if (!idsPerCalculatorFactory.containsKey(calculatorFactory)) {
                idsPerCalculatorFactory.put(calculatorFactory,new HashSet<ProcessingUnitStatisticsId>());
            }
            idsPerCalculatorFactory.get(calculatorFactory).add(statisticsId);
        }
        return idsPerCalculatorFactory;
    }
    
    private Map<InternalProcessingUnitStatisticsCalculatorClassProvider, Set<ProcessingUnitStatisticsId>> toIdsPerInstancesCalculator(
            ProcessingUnitStatisticsId[] statisticsIds) {
        final Map<InternalProcessingUnitStatisticsCalculatorClassProvider,Set<ProcessingUnitStatisticsId>> idsPerCalculatorFactory = new HashMap<InternalProcessingUnitStatisticsCalculatorClassProvider,Set<ProcessingUnitStatisticsId>>();
        
        for (final ProcessingUnitStatisticsId statisticsId : statisticsIds) {
            
            final InternalProcessingUnitStatisticsCalculatorClassProvider calculatorFactory = 
                    (InternalProcessingUnitStatisticsCalculatorClassProvider) statisticsId.getInstancesStatistics();
            if (!idsPerCalculatorFactory.containsKey(calculatorFactory)) {
                idsPerCalculatorFactory.put(calculatorFactory,new HashSet<ProcessingUnitStatisticsId>());
            }
            idsPerCalculatorFactory.get(calculatorFactory).add(statisticsId);
        }
        return idsPerCalculatorFactory;
    }

    private void calculateStatistics(
            final Map<InternalProcessingUnitStatisticsCalculatorClassProvider, Set<ProcessingUnitStatisticsId>> idsPerCalculatorFactory) {
        
        for (final Entry<InternalProcessingUnitStatisticsCalculatorClassProvider, Set<ProcessingUnitStatisticsId>> pair : idsPerCalculatorFactory.entrySet()) {
            
            Set<ProcessingUnitStatisticsId> statisticsIds = pair.getValue();
            InternalProcessingUnitStatisticsCalculator statisticsCalculator = statisticsCalculatorFactory.create(pair.getKey());
            statisticsCalculator.calculateNewStatistics(this, statisticsIds);
        }
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics#addStatistics(org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId, java.lang.Object)
     */
    @Override
    public void addStatistics(ProcessingUnitStatisticsId statisticsId, Object statisticsValue) {
        statistics.put(statisticsId, statisticsValue);
        
    }

}
