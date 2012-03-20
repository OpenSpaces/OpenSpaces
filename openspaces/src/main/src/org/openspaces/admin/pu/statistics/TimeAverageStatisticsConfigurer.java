package org.openspaces.admin.pu.statistics;

import java.util.concurrent.TimeUnit;

/**
 * Average all samples in the specified time window
 */
public class TimeAverageStatisticsConfigurer {

    AverageTimeWindowStatisticsConfig config = new AverageTimeWindowStatisticsConfig();

    TimeAverageStatisticsConfigurer timeWindow(long duration, TimeUnit timeUnit) {
        config.setTimeWindowSeconds(timeUnit.toSeconds(duration));
        return this;
    }
    
    public AverageTimeWindowStatisticsConfig create() {
        return config;
    }
}
