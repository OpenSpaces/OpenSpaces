package org.openspaces.admin.pu;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;

/**
 * Aggregates processing unit instance statistics over time
 * and generates new values (for example ["avg_threads":5.5, "max_threads":10 , "min_threads",1])
 * @author itaif
 *
 */
public interface ProcessingUnitInstanceStatisticsTimeAggregator {

    Map<String, Object> calcMonitors(ProcessingUnitInstanceStatistics instanceStatistics);
}
