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

package org.openspaces.admin.alerts.config;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.internal.alerts.bean.CpuUtilizationAlertBean;
import org.openspaces.core.util.StringProperties;

/**
 * A strongly typed machine CPU utilization alert bean configuration.
 * 
 * @see CpuUtilizationAlertBeanConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class CpuUtilizationAlertBeanConfig implements AlertBeanConfig {
    private static final long serialVersionUID = 1L;

    /**
     * Period of time (in milliseconds) the CPU is above/below a certain threshold to trigger an
     * alert; Recommended setting is 60000 ms (1 minute)
     */
    public static final String MEASUREMENT_PERIOD_MILLISECONDS_KEY = "measurement-period-milliseconds";

    /**
     * Low threshold for which to resolve a previously triggered CPU alert; Recommended setting is
     * 50% CPU utilization. An alert will be triggered if CPU goes below this threshold for a
     * certain period of time.
     */
    public static final String LOW_THRESHOLD_PERC_KEY = "low-threshold-perc";

    /**
     * High threshold for which to trigger an alert which needs resolution; Recommended setting is
     * above 80% CPU utilization. An alert will be triggered if CPU goes above this threshold for a
     * certain period of time.
     */
	public static final String HIGH_THRESHOLD_PERC_KEY = "high-threshold-perc";
	
	
	private final StringProperties properties = new StringProperties();

	/**
	 * Constructs an empty machine CPU utilization alert configuration.
	 */
	public CpuUtilizationAlertBeanConfig() {
	}
	
	/**
	 * Set the CPU high threshold percentage value.
	 * @param highThreshold high threshold percentage.
	 */
	public void setHighThresholdPerc(int highThreshold) {
	    properties.putInteger(HIGH_THRESHOLD_PERC_KEY, highThreshold);
	}
	
	/**
	 * @return the CPU high threshold percentage.
	 */
	public int getHighThresholdPerc() {
	    return Integer.valueOf(properties.get(HIGH_THRESHOLD_PERC_KEY)).intValue();
	}
	
	/**
	 * Set the CPU low threshold percentage value.
	 * @param lowThreshold low threshold percentage.
	 */
	public void setLowThresholdPerc(int lowThreshold) {
	    properties.putInteger(LOW_THRESHOLD_PERC_KEY, lowThreshold);
	}

	/**
	 * @return the CPU low threshold percentage.
	 */
	public int getLowThresholdPerc() {
	    return Integer.valueOf(properties.get(LOW_THRESHOLD_PERC_KEY)).intValue();
	}

    /**
     * Set the period of time a CPU alert should be triggered if it's reading is
     * above/below the threshold setting.
     * 
     * @param period
     *            period of time.
     * @param timeUnit
     *            the time unit of the specified period.
     */
	public void setMeasurementPeriod(long period, TimeUnit timeUnit) {
	    long periodInMilliseconds = timeUnit.toMillis(period);
	    properties.putLong(MEASUREMENT_PERIOD_MILLISECONDS_KEY, periodInMilliseconds);
	}
	
	/**
	 * @return the measurement period in milliseconds.
	 */
	public long getMeasurementPeriod() {
	    return Long.valueOf(properties.get(MEASUREMENT_PERIOD_MILLISECONDS_KEY)).longValue();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProperties(Map<String, String> properties) {
	    this.properties.clear();
	    this.properties.putAll(properties);
	}

	/**
     * {@inheritDoc}
     */
	public Map<String, String> getProperties() {
		return properties.getProperties();
	}

	/**
     * {@inheritDoc}
     */
	public String getBeanClassName() {
		return CpuUtilizationAlertBean.class.getName();
	}
}
