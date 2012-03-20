package org.openspaces.pu.service;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;

/**
 * Aggregates several {@link ProcessingUnitInstanceStatistics} into service monitors.
 * For example, an implementation could provide min / max / average of predefined instance service monitors.
 * @author itaif
 * @since 9.0.0
 */
public interface ProcessingUnitInstanceStatisticsClusterAggregator {
    
    /**
     * Aggregates the specified instance statistics into cluster-wide statistics.
     * The implementation can assume that the provided statistics have been sampled at approximately the same time.
     * @param instanceStatistics - the statistics objects per instance
     * @return Aggregated service monitors of the cluster
     */
    Map<String,Object> calcServiceMonitors(Map<ProcessingUnitInstance,ProcessingUnitInstanceStatistics> instanceStatistics);
}
