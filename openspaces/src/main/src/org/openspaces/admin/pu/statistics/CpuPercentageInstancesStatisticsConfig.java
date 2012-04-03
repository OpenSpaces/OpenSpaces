package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;

/**
 * Calculates the cpu percentage of all cluster instances values.
 * 
 * @since 9.0.0
 * @author itaif, gal
 * 
 */
public class CpuPercentageInstancesStatisticsConfig 
    extends AbstractTimeWindowStatisticsConfig 
    implements StatisticsObjectListFunction, InstancesStatisticsConfig {

    public CpuPercentageInstancesStatisticsConfig() {
        this(new HashMap<String, String>());
    }

    public CpuPercentageInstancesStatisticsConfig(Map<String, String> properties) {
        super(properties);
    }

    @Override
    public void validate() throws IllegalStateException {
        // ok
    }

    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getDeltaValuePerMilliSecond();
    }
}
