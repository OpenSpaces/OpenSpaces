package org.openspaces.admin.internal.pu;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
import org.openspaces.pu.service.ServiceMonitors;

/**
 * Provides an aggregation of the @{link {@link ProcessingUnitInstanceStatistics} service monitors
 * @since 9.0.0
 * @author itaif
 *
 */
public interface ProcessingUnitStatistics extends Iterable<ServiceMonitors> {

        /**
         * Returns a timestamp that is in sync with where the admin API is running. Can return
         * -1 if the clocks have are not sync yet.
         */
        long getAdminTimestamp();

        /**
         * @return a map of the {@link org.openspaces.pu.service.ServiceMonitors} per processing unit
         */
        Map<String, ServiceMonitors> getMonitors();
        
        /**
         * @return a map of {@link ProcessingUnitInstanceStatistics} per processing unit instance
         * that was used to generate this aggregated statistics.
         */
        Map <ProcessingUnitInstance,ProcessingUnitInstanceStatistics> getInstanceStatistics();
        
        /**
         * Returns the previous statistics.
         */
        ProcessingUnitStatistics getPrevious();
}
