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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculatorClassProvider;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculatorFactory;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.pu.service.ServiceMonitors;

public class DefaultProcessingUnitStatistics implements InternalProcessingUnitStatistics {

    private volatile ProcessingUnitStatistics lastStats;

    private final long adminTimestamp;
    
    private final Map<ProcessingUnitStatisticsId, Object> statistics;

    private final InternalProcessingUnitStatisticsCalculatorFactory statisticsCalculatorFactory;
    
    public DefaultProcessingUnitStatistics(
            long adminTimestamp, 
            ProcessingUnitStatistics lastStatistics,
            int historySize,
            InternalProcessingUnitStatisticsCalculatorFactory statisticsCalculatorFactory) {
    
        this.statisticsCalculatorFactory = statisticsCalculatorFactory;
        this.statistics = new HashMap<ProcessingUnitStatisticsId, Object>();
        this.adminTimestamp = adminTimestamp;
        this.lastStats = lastStatistics;
       
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultProcessingUnitStatistics)lastStats).lastStats = null;
        }
        
    }
    
    @Override
    public long getAdminTimestamp() {
        return this.adminTimestamp;
    }
    
    @Override
    public ProcessingUnitStatistics getPrevious() {
        return this.lastStats;
    }

    @Override
    public Map<ProcessingUnitStatisticsId, Object> getStatistics() {
        return Collections.unmodifiableMap(statistics);
    }

    @Override
    public void addStatistics(InternalProcessingUnitInstance instance) {
        ProcessingUnitInstanceStatistics lastInstanceStatistics = instance.getLastStatistics();
        if (lastInstanceStatistics != null) {
            Collection<ServiceMonitors> monitors = lastInstanceStatistics.getMonitors().values();
            for (ServiceMonitors serviceMonitors : monitors) {
                for (Map.Entry<String, Object> pair: serviceMonitors.getMonitors().entrySet()) {
                    
                    ProcessingUnitStatisticsId statisticsId = 
                            new ProcessingUnitStatisticsIdConfigurer()
                            .monitor(serviceMonitors.getId())
                            .metric(pair.getKey())
                            .instancesStatistics(new SingleInstanceStatisticsConfig(instance.getUid()))
                            .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
                            .create();
                    
                    statistics.put(statisticsId, pair.getValue());
                }
            }
        }
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
