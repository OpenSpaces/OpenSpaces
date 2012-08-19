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
import java.util.Map;

import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.ZoneStatisticsConfig;

/**
 * @author elip
 *
 */
public class ZoneStatisticsCalculator implements InternalProcessingUnitStatisticsCalculator {

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator#calculateNewStatistics(org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics, java.util.Collection)
     */
    @Override
    public void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics,
            Collection<ProcessingUnitStatisticsId> statisticIds) {
        
        // calculate new statistics for each request id.
        for (ProcessingUnitStatisticsId processingUnitStatisticsId : statisticIds) {
            calculateNewStatistics(processingUnitStatistics, processingUnitStatisticsId);
        }    
    } 
    
    private void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics, ProcessingUnitStatisticsId processingUnitStatisticsId) {
        
        // compare each request id with all current statistics entries.
        Map<ProcessingUnitStatisticsId, Object> statistics = processingUnitStatistics.getStatistics();
        for (Map.Entry<ProcessingUnitStatisticsId, Object> entry : statistics.entrySet()) {
            calculateNewStatistics(processingUnitStatistics, entry, processingUnitStatisticsId);
        }
        
    }
    
    private void calculateNewStatistics(InternalProcessingUnitStatistics internalProcessingUnitStatistics, Map.Entry<ProcessingUnitStatisticsId, Object> statisticsEntry, ProcessingUnitStatisticsId requestedProcessingUnitStatisticsId) {
        
        ProcessingUnitStatisticsId existingProcessingUnitStatisticsId = statisticsEntry.getKey();
        Object value = statisticsEntry.getValue();
        
        // zones to compare
        ZoneStatisticsConfig requestedZoneStatisticsConfig = requestedProcessingUnitStatisticsId.getZoneStatistics();
        ZoneStatisticsConfig existingZoneStatisticsConfig = existingProcessingUnitStatisticsId.getZoneStatistics();
        
        // keys without zones to compare
        ProcessingUnitStatisticsId erasedExistingProcessingUnitStatisticsId = erase(existingProcessingUnitStatisticsId);
        ProcessingUnitStatisticsId erasedRequestedProcessingUnitStatisticsId = erase(requestedProcessingUnitStatisticsId);
        
        if (requestedZoneStatisticsConfig.satisfiedBy(existingZoneStatisticsConfig)) {
            if (erasedRequestedProcessingUnitStatisticsId.equals(erasedExistingProcessingUnitStatisticsId)) {
                internalProcessingUnitStatistics.addStatistics(requestedProcessingUnitStatisticsId, value);
            }   
        }
    }
    
    private ProcessingUnitStatisticsId erase(ProcessingUnitStatisticsId processingUnitStatisticsId) {
        processingUnitStatisticsId.validate();
        ProcessingUnitStatisticsId erased = clone(processingUnitStatisticsId);
        erased.setZoneStatistics(new ErasedZonesStatisticsConfig());
        erased.setInstancesStatistics(new ErasedInstancesStatisticsConfig());
        return erased;
        
    }
    
    private ProcessingUnitStatisticsId clone(ProcessingUnitStatisticsId processingUnitStatisticsId) {
        return  new ProcessingUnitStatisticsIdConfigurer()
            .metric(processingUnitStatisticsId.getMetric())
            .monitor(processingUnitStatisticsId.getMonitor())
            .instancesStatistics(processingUnitStatisticsId.getInstancesStatistics())
            .timeWindowStatistics(processingUnitStatisticsId.getTimeWindowStatistics())
            .create();
    }    
}
