package org.openspaces.admin.internal.pu;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

public interface ProcessingUnitInstanceStatisticsCalculator {

    /**
     * Calculates processing unit instance statistics based on existing instance statistics
     * 
     * @param instance
     *            - The processing unit instance
     *            
     * @param instanceStatistics
     *            - The statistics of that instance that requires more calculation. Note that each
     *            statistics object is a node in a linked list which can be traversed with
     *            {@link ProcessingUnitInstanceStatistics#getPrevious()}
     */
    Map<ProcessingUnitStatisticsId,Object> calculate(ProcessingUnitInstance instance, ProcessingUnitInstanceStatistics instanceStatistics);
}
