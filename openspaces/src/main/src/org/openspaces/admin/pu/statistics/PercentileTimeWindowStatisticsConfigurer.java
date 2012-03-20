package org.openspaces.admin.pu.statistics;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.internal.pu.statistics.DefaultTimeWindowStatisticsConfigUtils;
/**
 * Fluent API for creating a new {@link PercentileTimeWindowStatisticsConfig} object
 * @author itaif
 * @since 9.0.0
 */
public class PercentileTimeWindowStatisticsConfigurer {
    
    private PercentileTimeWindowStatisticsConfig config = new PercentileTimeWindowStatisticsConfig();
    
    public PercentileTimeWindowStatisticsConfigurer timeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.timeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfigurer minimumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.minimumTimeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfigurer maximumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.maximumTimeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfigurer percentile(int percentile) {
        config.setPercentile(percentile);
        return this;
    }
    
    public PercentileTimeWindowStatisticsConfig create() {
        DefaultTimeWindowStatisticsConfigUtils.create(config);       
        return config;
    }
    
}
