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
import java.util.Iterator;
import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.pu.service.ProcessingUnitInstanceStatisticsClusterAggregator;
import org.openspaces.pu.service.ServiceMonitors;

public class DefaultProcessingUnitStatistics implements ProcessingUnitStatistics {

    private final Map<String, ServiceMonitors> serviceMonitorsById;

    private volatile ProcessingUnitStatistics lastStats;

    private final Map<ProcessingUnitInstance, ProcessingUnitInstanceStatistics> processingUnitInstanceStatistics;
    
    public DefaultProcessingUnitStatistics(
            Map<ProcessingUnitInstance,ProcessingUnitInstanceStatistics> processingUnitInstanceStatistics,
            Map<String, ProcessingUnitInstanceStatisticsClusterAggregator[]> clusterAggregatorsById,
            ProcessingUnitStatistics previous,
            int historySize) {
        
        this.processingUnitInstanceStatistics = processingUnitInstanceStatistics;
        this.lastStats = previous;
       
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultProcessingUnitStatistics)lastStats).lastStats = null;
        }
        
        this.serviceMonitorsById = new HashMap<String, ServiceMonitors>();
    }
    
    @Override
    public long getAdminTimestamp() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public Iterator<ServiceMonitors> iterator() {
        return Collections.unmodifiableCollection(serviceMonitorsById.values()).iterator();
    }

    @Override
    public Map<String, ServiceMonitors> getMonitors() {
        return Collections.unmodifiableMap(this.serviceMonitorsById);
    }

    @Override
    public Map<ProcessingUnitInstance, ProcessingUnitInstanceStatistics> getInstanceStatistics() {
        return Collections.unmodifiableMap(processingUnitInstanceStatistics);
    }

    @Override
    public ProcessingUnitStatistics getPrevious() {
        return this.lastStats;
    }

}
