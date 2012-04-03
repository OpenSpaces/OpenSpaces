package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;

/**
 * Calculates the cpu percentage by dividing the total CPU values by delta time passed in milliseconds.
 * 
 * @since 9.0.0
 * @author gal
 * 
 */
public class CpuPercentageTimeWindowStatisticsConfig 
    extends AbstractTimeWindowStatisticsConfig 
    implements StatisticsObjectListFunction, InstancesStatisticsConfig {

    public CpuPercentageTimeWindowStatisticsConfig() {
        this(new HashMap<String, String>());
    }

    public CpuPercentageTimeWindowStatisticsConfig(Map<String, String> properties) {
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
