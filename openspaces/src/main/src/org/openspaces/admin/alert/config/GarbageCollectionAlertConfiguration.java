/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.alert.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.internal.alert.bean.GarbageCollectionAlertBean;

/**
 * A Garbage Collection pause alert configuration. Specifies the thresholds for triggering an alert. There are
 * two thresholds, long period and short period indicating how long a gc took. The garbage collection alert
 * is raised if the gc took longer than the specified 'long' period. The garbage collection alert is resolved
 * if gc took less than the specified 'short' period.
 * 
 * @see GarbageCollectionAlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class GarbageCollectionAlertConfiguration implements AlertConfiguration {
    private static final long serialVersionUID = 1L;
        
    /**
     * Period of time (in milliseconds) the JVM has spent on garbage collection, for which to
     * raise an alert.
     */
    public static final String LONG_GC_PAUSE_PERIOD_MILLISECONDS_KEY = "long-gc-pause-period-milliseconds";

    /**
     * Period of time (in milliseconds) the JVM has spent on garbage collection, for which to
     * resolve an alert.
     */
    public static final String SHORT_GC_PAUSE_PERIOD_MILLISECONDS_KEY = "short-gc-pause-period-milliseconds";
    
    
	private final Map<String, String> properties = new HashMap<String, String>();

    private boolean enabled;

	/**
	 * Constructs an empty garbage collection pause alert configuration.
	 */
	public GarbageCollectionAlertConfiguration() {
	}
	
    /**
     * Set the period of time a long GC pause alert should be raised for.
     * 
     * @param period
     *            period of time spent on GC.
     * @param timeUnit
     *            the time unit of the specified period.
     */
	public void setLongGcPausePeriod(long period, TimeUnit timeUnit) {
	    long periodInMilliseconds = timeUnit.toMillis(period);
        properties.put(LONG_GC_PAUSE_PERIOD_MILLISECONDS_KEY, String.valueOf(periodInMilliseconds));
	}
	
	/**
	 * @return the long GC pause period in milliseconds.
	 */
	public Long getLongGcPausePeriod() {
	    String value = properties.get(LONG_GC_PAUSE_PERIOD_MILLISECONDS_KEY);
	    if (value == null) return null;
	    return Long.valueOf(value);
	}
	
    /**
     * Set the period of time a GC pause alert should be resolved for.
     * 
     * @param period
     *            period of time spent on GC.
     * @param timeUnit
     *            the time unit of the specified period.
     */
    public void setShortGcPausePeriod(long period, TimeUnit timeUnit) {
        long periodInMilliseconds = timeUnit.toMillis(period);
        properties.put(SHORT_GC_PAUSE_PERIOD_MILLISECONDS_KEY, String.valueOf(periodInMilliseconds));
    }
    
    /**
     * @return the low GC pause period in milliseconds.
     */
    public Long getShortGcPausePeriod() {
        String value = properties.get(SHORT_GC_PAUSE_PERIOD_MILLISECONDS_KEY);
        if (value == null) return null;
        return Long.valueOf(value);
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void setProperties(Map<String, String> properties) {
	    this.properties.clear();
	    this.properties.putAll(properties);
	}

	/**
     * {@inheritDoc}
     */
	@Override
    public Map<String, String> getProperties() {
		return properties;
	}

	/**
     * {@inheritDoc}
     */
	@Override
    public String getBeanClassName() {
		return GarbageCollectionAlertBean.class.getName();
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
