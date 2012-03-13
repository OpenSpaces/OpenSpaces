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
package org.openspaces.pu.service;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatisticsTimeAggregator;
import org.openspaces.pu.service.CalculatedServiceMonitors;

/**
 * Aggregates raw service monitor samples using time based statistics
 * @author itaif
 * @see ProcessingUnitInstanceStatisticsTimeAggregator
 */
public class TimeAggregatedServiceMonitors extends CalculatedServiceMonitors {

    private final ProcessingUnitInstanceStatisticsTimeAggregator[] aggregators;
    
    private final ProcessingUnitInstanceStatistics instanceStatistics;
    
    public TimeAggregatedServiceMonitors(
            String id, 
            ProcessingUnitInstanceStatisticsTimeAggregator[] aggregators, 
            ProcessingUnitInstanceStatistics instanceStatistics) {
        super(id);
        this.aggregators = aggregators;
        this.instanceStatistics = instanceStatistics;
        
    }

    @Override
    protected Map<String,Object> calcMonitors() {
        
        Map<String, Object> output = new HashMap<String,Object>(); 
        for (ProcessingUnitInstanceStatisticsTimeAggregator aggregatedMonitor: aggregators ) {
            
            output.putAll(
                    aggregatedMonitor.calcMonitors(instanceStatistics));
        }
        return output;
    }
}
