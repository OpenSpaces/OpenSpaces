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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.statistics.InstancesStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;
import org.openspaces.admin.internal.pu.statistics.TimeWindowStatisticsCalculator;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfigurer;

public class DefaultProcessingUnitStatistics implements InternalProcessingUnitStatistics {

    private volatile ProcessingUnitStatistics previous;

    private final long adminTimestamp;
    
    private final Map<ProcessingUnitStatisticsId, Object> statistics;

    private final InternalProcessingUnitStatisticsCalculator timeWindowStatisticsCalculator = new TimeWindowStatisticsCalculator();
    private final InternalProcessingUnitStatisticsCalculator instancesStatisticsCalculator = new InstancesStatisticsCalculator();

    private Log logger = LogFactory.getLog(this.getClass());

    public DefaultProcessingUnitStatistics(
            long adminTimestamp, 
            ProcessingUnitStatistics lastStatistics,
            int historySize) {
    
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
    public void addStatistics(ProcessingUnitStatisticsId statisticsId, Object statisticsValue) {
        statistics.put(statisticsId, statisticsValue);
        
    }

    @Override
    public void calculateStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds, Set<String> instancesUid) {
            
        calculateTimeWindowStatistics(statisticsIds, instancesUid);

        calculateInstancesStatistics(statisticsIds);
    }

    private void calculateInstancesStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds) {
        
        final List<ProcessingUnitStatisticsId> instancesCalculatedStatistics = new ArrayList<ProcessingUnitStatisticsId>();
        for (final ProcessingUnitStatisticsId statisticsId : statisticsIds) {
            if (statisticsId.getInstancesStatistics() instanceof StatisticsObjectListFunction) {
                instancesCalculatedStatistics.add(statisticsId);
            }
        }
    
        instancesStatisticsCalculator.calculateNewStatistics(this, instancesCalculatedStatistics);
    }

    private void calculateTimeWindowStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds, Set<String> instancesUid) {
        
        final List<ProcessingUnitStatisticsId> singleInstanceCalculatedStatistics = new ArrayList<ProcessingUnitStatisticsId>();
        
        for (final ProcessingUnitStatisticsId statisticsId : statisticsIds) {
            if (statisticsId.getInstancesStatistics() instanceof SingleInstanceStatisticsConfig) {
                // instance UID is already specified. Just check that it is still discovered
                final String instanceUid = ((SingleInstanceStatisticsConfig)(statisticsId.getInstancesStatistics())).getInstanceUid();
                if (instancesUid.contains(instanceUid)) {
                    singleInstanceCalculatedStatistics.add(statisticsId);
                }
                else {
                    if (logger .isDebugEnabled()) {
                        logger.debug("Failed to find instance UID " + instanceUid);
                    }
                }
            }
            else {
                //expand to all instance UIDs
                for (final String instanceUid : instancesUid) {
                    singleInstanceCalculatedStatistics.add(
                            new ProcessingUnitStatisticsIdConfigurer()
                            .monitor(statisticsId.getMonitor())
                            .metric(statisticsId.getMetric())
                            .timeWindowStatistics(statisticsId.getTimeWindowStatistics())
                            .instancesStatistics(new SingleInstanceStatisticsConfigurer().instanceUid(instanceUid).create())
                            .create());
                }
            }
        }
        
        timeWindowStatisticsCalculator.calculateNewStatistics(this, singleInstanceCalculatedStatistics);
    }

}
