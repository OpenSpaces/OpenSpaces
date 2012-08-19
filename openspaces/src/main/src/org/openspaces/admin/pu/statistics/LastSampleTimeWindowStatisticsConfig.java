package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.config.AbstractConfig;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;

public class LastSampleTimeWindowStatisticsConfig
        extends AbstractConfig
        implements TimeWindowStatisticsConfig , StatisticsObjectListFunction {
   
    public LastSampleTimeWindowStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public LastSampleTimeWindowStatisticsConfig(Map<String, String> properties) {
        super(properties);
    }

    @Override
    public void validate() throws IllegalStateException {
        // ok
    }
    
    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getLast();
    }

    @Override
    public int getMaxNumberOfSamples(long statisticsPollingInterval, TimeUnit timeUnit) {
        return 1;
    }

}
