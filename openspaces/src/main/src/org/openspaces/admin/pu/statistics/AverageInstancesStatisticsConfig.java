package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractInstancesStatisticsConfig;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;

/**
 * Calculates the average of all cluster instances values. 
 * @since 9.0.0
 * @author itaif
 *
 */
public class AverageInstancesStatisticsConfig extends AbstractInstancesStatisticsConfig implements StatisticsObjectListFunction {

    @Override
    public String toString() {
        return "averageInstancesStatistics";
    }

    @Override
    public void validate() throws IllegalStateException {
        //ok
    }

    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getAverage();
    }
    
}
