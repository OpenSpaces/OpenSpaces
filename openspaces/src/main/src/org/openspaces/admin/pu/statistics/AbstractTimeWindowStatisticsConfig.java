/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.pu.statistics;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.config.AbstractConfig;

/**
 * Base class for statistics configurations that aggregate samples based on a specified time window
 * @author itaif
 * @since 9.0.0
 */
public abstract class AbstractTimeWindowStatisticsConfig 
    extends AbstractConfig
    implements TimeWindowStatisticsConfig {

    protected AbstractTimeWindowStatisticsConfig(Map<String, String> properties) {
        super(properties);
    }

    private static final String TIMEWINDOW_SECONDS_KEY = "time-window-seconds";
    private static final long TIMEWINDOW_SECONDS_DEFAULT = 60;
    private static final String MINIMUM_TIMEWINDOW_SECONDS_KEY = "minimum-time-window-seconds";
    private static final String MAXIMUM_TIMEWINDOW_SECONDS_KEY = "maximum-time-window-seconds";
    
    /**
     * @return the timeWindowSeconds
     */
    public Long getTimeWindowSeconds() {
        return getStringProperties().getLong(TIMEWINDOW_SECONDS_KEY, TIMEWINDOW_SECONDS_DEFAULT);
    }
    /**
     * @param timeWindowSeconds the timeWindowSeconds to set
     */
    public void setTimeWindowSeconds(long timeWindowSeconds) {
        getStringProperties().putLong(TIMEWINDOW_SECONDS_KEY, timeWindowSeconds);
    }
    /**
     * @return the minimumTimeWindowSeconds
     */
    public Long getMinimumTimeWindowSeconds() {
        return getStringProperties().getLong(MINIMUM_TIMEWINDOW_SECONDS_KEY, getTimeWindowSeconds());
    }
    
    /**
     * @param minimumTimeWindowSeconds the minimumTimeWindowSeconds to set
     */
    public void setMinimumTimeWindowSeconds(long minimumTimeWindowSeconds) {
        getStringProperties().putLong(MINIMUM_TIMEWINDOW_SECONDS_KEY, minimumTimeWindowSeconds);
    }
    /**
     * @return the maximumTimeWindowSeconds
     */
    public Long getMaximumTimeWindowSeconds() {
        return getStringProperties().getLong(MAXIMUM_TIMEWINDOW_SECONDS_KEY, getDefaultMaximumTimeWindowSeconds());
    }

    private long getDefaultMaximumTimeWindowSeconds() {
        // allow the last sample to sneak into the time window even if the
        // timestamp has jitter.
        return getTimeWindowSeconds()*2;
    }
    
    /**
     * @param maximumTimeWindowSeconds the maximumTimeWindowSeconds to set
     */
    public void setMaximumTimeWindowSeconds(long maximumTimeWindowSeconds) {
        getStringProperties().putLong(MAXIMUM_TIMEWINDOW_SECONDS_KEY, maximumTimeWindowSeconds);
    }
    
    public void validate() throws IllegalStateException {
        
        if (getTimeWindowSeconds()<=0) {
            throw new IllegalStateException("timeWindowSeconds must be positive");
        }
        
        if (getMinimumTimeWindowSeconds() <0) {
            throw new IllegalStateException("minimumTimeWindowSeconds must not be negative");
        }
        
        if (getMaximumTimeWindowSeconds() <=0) {
            throw new IllegalStateException("maximumTimeWindowSeconds must be positive");
        }
        
        if (getMaximumTimeWindowSeconds() < getTimeWindowSeconds()) {
            throw new IllegalStateException("maximumTimeWindowSeconds ("+getMaximumTimeWindowSeconds()+") must be bigger or equals timeWindowSeconds ("+ getTimeWindowSeconds()+")");
        }
        
        if (getMinimumTimeWindowSeconds() > getTimeWindowSeconds()) {
            throw new IllegalStateException("minimumTimeWindowSeconds must be less or equals timeWindowSeconds");
        }
    }
    

    @Override
    public int getMaxNumberOfSamples(long statisticsPollingInterval, TimeUnit timeUnit) {
        long intervalSeconds = timeUnit.toSeconds(statisticsPollingInterval);
        
        // #intervals = timewindow / interval
        int numberOfPollingIntervals =
                (int) Math.ceil(1.0*getMaximumTimeWindowSeconds()/intervalSeconds);

        // number of samples is always one more than number of intervals
        return 1 + numberOfPollingIntervals;
    }

}
