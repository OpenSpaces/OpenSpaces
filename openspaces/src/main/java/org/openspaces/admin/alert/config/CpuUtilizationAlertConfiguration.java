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

import org.openspaces.admin.internal.alert.bean.CpuUtilizationAlertBean;

/**
 * A CPU utilization alert configuration. Specifies the thresholds for triggering an alert. There are
 * two thresholds, high and low and a measurement period indicating a window for the CPU reading.
 * The CPU alert is raised if any discovered machine is above the specified CPU threshold for a
 * period of time. The CPU alert is resolved if its CPU goes below the specified CPU threshold for a
 * period of time.
 * 
 * @see CpuUtilizationAlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class CpuUtilizationAlertConfiguration implements AlertConfiguration {
    private static final long serialVersionUID = 1L;

    /**
     * Period of time (in milliseconds) the CPU is above/below a certain threshold to trigger an
     * alert.
     */
    public static final String MEASUREMENT_PERIOD_MILLISECONDS_KEY = "measurement-period-milliseconds";

    /**
     * Low threshold for which to resolve a previously triggered CPU alert. An alert will be
     * triggered if CPU goes below this threshold for a certain period of time.
     */
    public static final String LOW_THRESHOLD_PERC_KEY = "low-threshold-perc";

    /**
     * High threshold for which to trigger an alert which needs resolution. An alert will be
     * triggered if CPU goes above this threshold for a certain period of time.
     */
	public static final String HIGH_THRESHOLD_PERC_KEY = "high-threshold-perc";
	
	
	private final Map<String,String> properties = new HashMap<String, String>();

    private boolean enabled;

	/**
	 * Constructs an empty machine CPU utilization alert configuration.
	 */
	public CpuUtilizationAlertConfiguration() {
	}
	
	/**
	 * Set the CPU high threshold percentage value.
	 * @param highThreshold high threshold percentage.
	 */
	public void setHighThresholdPerc(int highThreshold) {
	    properties.put(HIGH_THRESHOLD_PERC_KEY, String.valueOf(highThreshold));
	}
	
	/**
	 * @return the CPU high threshold percentage.
	 */
	public Integer getHighThresholdPerc() {
	    String value = properties.get(HIGH_THRESHOLD_PERC_KEY);
	    if (value == null) return null;
	    return Integer.valueOf(value);
	}
	
	/**
	 * Set the CPU low threshold percentage value.
	 * @param lowThreshold low threshold percentage.
	 */
	public void setLowThresholdPerc(int lowThreshold) {
	    properties.put(LOW_THRESHOLD_PERC_KEY, String.valueOf(lowThreshold));
	}

	/**
	 * @return the CPU low threshold percentage.
	 */
	public Integer getLowThresholdPerc() {
	    String value = properties.get(LOW_THRESHOLD_PERC_KEY);
        if (value == null) return null;
        return Integer.valueOf(value);
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
	    properties.put(MEASUREMENT_PERIOD_MILLISECONDS_KEY, String.valueOf(periodInMilliseconds));
	}
	
	/**
	 * @return the measurement period in milliseconds.
	 */
	public Long getMeasurementPeriod() {
	    String value = properties.get(MEASUREMENT_PERIOD_MILLISECONDS_KEY);
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
		return CpuUtilizationAlertBean.class.getName();
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
