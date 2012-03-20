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
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.InstancesStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.TimeWindowStatisticsCalculator;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

public class DefaultProcessingUnitStatistics implements InternalProcessingUnitStatistics {

    private volatile ProcessingUnitStatistics previous;

    private final long adminTimestamp;
    
    private final Map<ProcessingUnitStatisticsId, Object> statistics;

    private final InternalProcessingUnitStatisticsCalculator timeWindowStatisticsCalculator = new TimeWindowStatisticsCalculator();
    private final InternalProcessingUnitStatisticsCalculator instancesStatisticsCalculator = new InstancesStatisticsCalculator();

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
    public void calculateStatistics(ProcessingUnitStatisticsId[] statisticsIds) {
        
        // first aggregate each instances statistics individually as a function of time
        timeWindowStatisticsCalculator.calculateNewStatistics(this, statisticsIds);
        
        // then aggregate all instances statistics (measured and calculated) into a single cluster value
        instancesStatisticsCalculator.calculateNewStatistics(this, statisticsIds);
    }

    @Override
    public void addStatistics(ProcessingUnitStatisticsId statisticsId, Object statisticsValue) {
        statistics.put(statisticsId, statisticsValue);
        
    }

}
