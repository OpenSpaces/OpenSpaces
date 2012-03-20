package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractInstancesStatisticsConfig;

/**
 * Calculates the average of all cluster instances values. 
 * @since 9.0.0
 * @author itaif
 *
 */
public class AverageInstancesStatisticsConfig extends AbstractInstancesStatisticsConfig {

    @Override
    public String toString() {
        return "averageInstancesStatistics";
    }

    @Override
    public void validate() throws IllegalStateException {
        //ok
    }
    
}
