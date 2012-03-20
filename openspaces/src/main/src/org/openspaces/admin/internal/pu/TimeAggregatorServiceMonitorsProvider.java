package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.pu.service.ServiceMonitors;

public interface TimeAggregatorServiceMonitorsProvider {

    /**
     * Aggregates the specified processing unit instance statistics and returns the result in a form of service monitors.
     * Note that each statistics object is a node in a linked list which can be traversed with {@link ProcessingUnitInstanceStatistics#getPrevious()}
     */
    ServiceMonitors[] aggregate(ProcessingUnitInstanceStatistics instanceStatistics);
}
