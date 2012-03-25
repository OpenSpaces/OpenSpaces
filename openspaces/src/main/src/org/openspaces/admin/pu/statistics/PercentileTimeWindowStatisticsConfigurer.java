package org.openspaces.admin.pu.statistics;

import java.util.concurrent.TimeUnit;
/**
 * Fluent API for creating a new {@link PercentileTimeWindowStatisticsConfig} object
 * @author itaif
 * @since 9.0.0
 */
public class PercentileTimeWindowStatisticsConfigurer {
    
    private PercentileTimeWindowStatisticsConfig config = new PercentileTimeWindowStatisticsConfig();
    
    public PercentileTimeWindowStatisticsConfigurer timeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfigurer minimumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setMinimumTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfigurer maximumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setMaximumTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfigurer percentile(int percentile) {
        config.setPercentile(percentile);
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfig create() {
        config.validate();       
        return config;
    }
    
}
