package org.openspaces.admin.internal.pu;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

/**
 * Provides an aggregation of the @{link {@link ProcessingUnitInstanceStatistics} service monitors
 * 
 * @since 9.0.0
 * @author itaif
 * 
 */
public interface ProcessingUnitStatistics {

    /**
     * @return a timestamp that is in sync with where the admin API is running. Can return -1 if the
     *         clocks have are not sync yet.
     */
    long getAdminTimestamp();

    /**
     * @return the statistics value for a single instance or aggregation of the complete cluster.
     *         The value can be Double , Number, String or any other type (Object).
     * 
     * @see ProcessingUnitStatisticsId
     * 
     */
    Map<ProcessingUnitStatisticsId, Object> getStatistics();

    /**
     * @return the previous processing unit statistics
     */
    ProcessingUnitStatistics getPrevious();

}
