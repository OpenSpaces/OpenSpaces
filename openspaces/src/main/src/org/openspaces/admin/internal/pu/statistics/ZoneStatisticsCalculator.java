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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;

/**
 * @author elip
 *
 */
public class ZoneStatisticsCalculator implements InternalProcessingUnitStatisticsCalculator {
    
    private final Log logger = LogFactory.getLog(this.getClass());

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator#calculateNewStatistics(org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics, java.util.Collection)
     */
    @Override
    public void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics,
            Collection<ProcessingUnitStatisticsId> requestedStatisticsIds) {
        
        if (logger.isTraceEnabled()) {
            logger.trace("calculateNewStatistics(processingUnitStatistics="+processingUnitStatistics+" , statisticsIds="+ requestedStatisticsIds + " admin timestampe = " + processingUnitStatistics.getAdminTimestamp());
        }
        
        //copy to avoid conc. modification exception on statistics
        Map<ProcessingUnitStatisticsId, Object> statistics = new HashMap<ProcessingUnitStatisticsId, Object>(processingUnitStatistics.getStatistics());
        
        // calculate new statistics for each request id.
        for (ProcessingUnitStatisticsId requestedStatisticsId : requestedStatisticsIds) {
            calculateNewStatistics(processingUnitStatistics, statistics, requestedStatisticsId);
        } 
        if (logger.isTraceEnabled()) {
            logger.trace("calculateNewStatistics(processingUnitStatistics="+processingUnitStatistics+" , statisticsIds="+ requestedStatisticsIds + " admin timestampe = " + processingUnitStatistics.getAdminTimestamp());
        }
    } 
    
    private void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics, Map<ProcessingUnitStatisticsId, Object> copyOfStatistics , ProcessingUnitStatisticsId requestedStatisticsId) {
        requestedStatisticsId.validate();
        
        // iterate over the copy of the original statistics
        boolean statisfied = false;
        for (Map.Entry<ProcessingUnitStatisticsId, Object> statisticsEntry : copyOfStatistics.entrySet()) {
            if (calculateNewStatistics(processingUnitStatistics, statisticsEntry, requestedStatisticsId)) {
                statisfied = true;
            }
        }
        if (!statisfied && logger.isTraceEnabled()) {
            logger.trace("requestedZoneStatisticsConfig : " + requestedStatisticsId.getAgentZones() + " is not satisfied by any existing statisitcs");
        }
        
    }
    
    private boolean calculateNewStatistics(InternalProcessingUnitStatistics internalProcessingUnitStatistics, Map.Entry<ProcessingUnitStatisticsId, Object> statisticsEntry, ProcessingUnitStatisticsId requestedStatisticsId) {
        
        ProcessingUnitStatisticsId existingProcessingUnitStatisticsId = statisticsEntry.getKey();
        Object value = statisticsEntry.getValue();
        
        // zones to compare
        ZonesConfig requestedZoneStatisticsConfig = requestedStatisticsId.getAgentZones();
        // keys without zones to compare
        
        ProcessingUnitStatisticsId erasedExistingProcessingUnitStatisticsId = erase(existingProcessingUnitStatisticsId);
        ProcessingUnitStatisticsId erasedrequestedStatisticsId = erase(requestedStatisticsId);
        
        ExactZonesConfig existingZoneStatisticsConfig = null;
        if (existingProcessingUnitStatisticsId.getAgentZones() instanceof ExactZonesConfig) {
            existingZoneStatisticsConfig = (ExactZonesConfig) existingProcessingUnitStatisticsId.getAgentZones(); // there may be zones config that are not ExactZones
        } else {
            throw new IllegalStateException("found a map entry with key = " + existingProcessingUnitStatisticsId + " and value = " + value + " in processing unti statistics = " + internalProcessingUnitStatistics + ". ZonesConfig for this entry is not an instance of ExactZonesConfig." + "requestedStatisticsId for calculation was = " + requestedStatisticsId);
        }
            
        if (requestedZoneStatisticsConfig.isSatisfiedBy(existingZoneStatisticsConfig)) {
               
            if (erasedrequestedStatisticsId.equals(erasedExistingProcessingUnitStatisticsId)) {
                ProcessingUnitStatisticsId newProcessingUnitStatisticsId = existingProcessingUnitStatisticsId.shallowClone();
                requestedZoneStatisticsConfig.validate();
                newProcessingUnitStatisticsId.setAgentZones(requestedZoneStatisticsConfig);
                // add to the actual statistics
                internalProcessingUnitStatistics.addStatistics(newProcessingUnitStatisticsId, value);
                return true;
            }
        } 
        return false;
    }
    
    private ProcessingUnitStatisticsId erase(ProcessingUnitStatisticsId processingUnitStatisticsId) {
        processingUnitStatisticsId.validate();
        ProcessingUnitStatisticsId erased = processingUnitStatisticsId.shallowClone();
        erased.setAgentZones(null);
        erased.setInstancesStatistics(null);
        return erased;
        
    }
}
