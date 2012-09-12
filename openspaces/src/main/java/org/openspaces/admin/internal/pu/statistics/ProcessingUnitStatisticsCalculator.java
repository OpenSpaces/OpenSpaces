package org.openspaces.admin.internal.pu.statistics;

import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;

public interface ProcessingUnitStatisticsCalculator {

    /**
     * Calculates new processing unit statistics from existing instance statistics
     * 
     * @param instanceStatistics
     *            statistics for each instance. Note that each statistics object is a node in a
     *            linked list which can be traversed with
     *            {@link ProcessingUnitStatistics#getPrevious()}
     *            New statistics should be added using {@link InternalProcessingUnitStatistics#addStatistics()} 
     */
    void enrich(InternalProcessingUnitStatistics statistics);
}
