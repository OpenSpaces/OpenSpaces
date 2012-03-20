package org.openspaces.admin.pu.statistics;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.internal.pu.statistics.DefaultTimeWindowStatisticsConfigUtils;

public class PercentileTimeWindowStatisticsConfigurer {
    
    private PercentileTimeWindowStatisticsConfig config = new PercentileTimeWindowStatisticsConfig();
    
    PercentileTimeWindowStatisticsConfigurer timeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.timeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    PercentileTimeWindowStatisticsConfigurer minimumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.minimumTimeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    PercentileTimeWindowStatisticsConfigurer maximumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.maximumTimeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    PercentileTimeWindowStatisticsConfigurer percentile(int percentile) {
        config.setPercentile(percentile);
        return this;
    }
    
    PercentileTimeWindowStatisticsConfig create() {
        DefaultTimeWindowStatisticsConfigUtils.create(config);       
        return config;
    }
    
}
