package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractInstancesStatisticsConfig;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;

/**
 * Picks the minimum of all cluster instances values. 
 * @since 9.0.0
 * @author itaif
 *
 */
public class MaximumInstancesStatisticsConfig 
                extends AbstractInstancesStatisticsConfig 
                implements StatisticsObjectListFunction {

    @Override
    public String toString() {
        return "maximumInstancesStatistics";
    }

    @Override
    public void validate() throws IllegalStateException {
        //ok
    }

    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getMaximum();
    }
    
}