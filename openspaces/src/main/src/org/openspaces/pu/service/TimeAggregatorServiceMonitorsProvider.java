package org.openspaces.pu.service;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;

public interface TimeAggregatorServiceMonitorsProvider {

    /**
     * Aggregates the specified processing unit instance statistics and returns the result in a form of service monitors.
     * Note that each statistics object is a node in a linked list which can be traversed with {@link ProcessingUnitInstanceStatistics#getPrevious()}
     */
    ServiceMonitors[] aggregate(ProcessingUnitInstanceStatistics instanceStatistics);
    
    @Override
    public boolean equals(Object obj);
    
    @Override
    public int hashCode();
}
