package org.openspaces.admin.internal.pu;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.pu.service.ServiceMonitors;

public interface ClusterAggregatorServiceMonitorsProvider {

    /**
     * Aggregates the specified processing unit instance statistics and returns the result in a form of service monitors.
     * Note that each statistics object is a node in a linked list which can be traversed with {@link ProcessingUnitInstanceStatistics#getPrevious()}
     */
    ServiceMonitors[] aggregate(Map<ProcessingUnitInstance,ProcessingUnitInstanceStatistics> instancesStatistics);
}
