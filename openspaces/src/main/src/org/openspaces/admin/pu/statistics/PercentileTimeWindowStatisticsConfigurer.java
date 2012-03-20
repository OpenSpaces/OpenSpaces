package org.openspaces.admin.pu.statistics;

import java.util.concurrent.TimeUnit;

public class PercentileTimeWindowStatisticsConfigurer {
    
    private PercentileTimeWindowStatisticsConfig config = new PercentileTimeWindowStatisticsConfig();
    
    PercentileTimeWindowStatisticsConfigurer timeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
    
    PercentileTimeWindowStatisticsConfigurer percentile(int percentile) {
        config.setPercentile(percentile);
        return this;
    }
    
    PercentileTimeWindowStatisticsConfig create() {
        return config;
    }
    
}
