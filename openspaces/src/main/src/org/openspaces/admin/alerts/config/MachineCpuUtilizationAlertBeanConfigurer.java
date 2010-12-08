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

import java.util.concurrent.TimeUnit;

/**
 * A strongly typed machine CPU utilization alert bean configurer.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class MachineCpuUtilizationAlertBeanConfigurer implements AlertBeanConfigurer {

	private final MachineCpuUtilizationAlertBeanConfig config = new MachineCpuUtilizationAlertBeanConfig();
	
	/**
	 * Constructs an empty machine CPU utilization alert configuration.
	 */
	public MachineCpuUtilizationAlertBeanConfigurer() {
	}
	
	/**
	 * Set the CPU high threshold percentage value.
	 * @param highThreshold high threshold percentage.
	 * @return this.
	 */
	public MachineCpuUtilizationAlertBeanConfigurer highThresholdPerc(int highThreshold) {
		config.setHighThresholdPerc(highThreshold);
		return this;
	}
	
	/**
	 * Set the CPU low threshold percentage value.
	 * @param lowThreshold low threshold percentage.
	 * @return this.
	 */
	public MachineCpuUtilizationAlertBeanConfigurer lowThresholdPerc(int lowThreshold) {
		config.setLowThresholdPerc(lowThreshold);
		return this;
	}

    /**
     * Set the period of time a CPU alert should be triggered if it's reading is above/below the
     * threshold setting.
     * 
     * @param period
     *            period of time.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
	public MachineCpuUtilizationAlertBeanConfigurer measurementPeriod(long period, TimeUnit timeUnit) {
		config.setMeasurementPeriod(period, timeUnit);
		return this;
	}
	
	/**
	 * Get a fully configured machine CPU utilization configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public MachineCpuUtilizationAlertBeanConfig getConfig() {
		return config;
	}
}
