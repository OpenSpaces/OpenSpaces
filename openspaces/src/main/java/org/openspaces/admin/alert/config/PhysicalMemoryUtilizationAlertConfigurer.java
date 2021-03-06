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

import java.util.concurrent.TimeUnit;

/**
 * A physical memory utilization alert configurer. Specifies the thresholds for triggering an alert.
 * There are two thresholds, high and low and a measurement period indicating a window for the
 * memory reading. The memory utilization alert is raised if any discovered machine is above the
 * specified physical memory threshold for a period of time. The memory utilization alert is
 * resolved if its physical memory reading goes below the specified memory threshold for a period of
 * time.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link PhysicalMemoryUtilizationAlertConfiguration} configuration.
 * 
 * @see PhysicalMemoryUtilizationAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class PhysicalMemoryUtilizationAlertConfigurer implements AlertConfigurer {

	private final PhysicalMemoryUtilizationAlertConfiguration config = new PhysicalMemoryUtilizationAlertConfiguration();
	
	/**
	 * Constructs an empty machine physical memory utilization alert configuration.
	 */
	public PhysicalMemoryUtilizationAlertConfigurer() {
	}
	
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    @Override
    public PhysicalMemoryUtilizationAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }

    /**
     * Raise an alert if physical memory is above the specified threshold for a period of time. The
     * period of time is configured using {@link #measurementPeriod(long, TimeUnit)}.
     * 
     * @see PhysicalMemoryUtilizationAlertConfiguration#setHighThresholdPerc(int)
     * 
     * @param highThreshold
     *            high threshold percentage.
     * @return this.
     */
	public PhysicalMemoryUtilizationAlertConfigurer raiseAlertIfMemoryAbove(int highThreshold) {
		config.setHighThresholdPerc(highThreshold);
		return this;
	}

    /**
     * Resolve the previously raised alert if physical memory goes below the specified threshold for
     * a period of time. The period of time is configured using
     * {@link #measurementPeriod(long, TimeUnit)}.
     * 
     * @see PhysicalMemoryUtilizationAlertConfiguration#setLowThresholdPerc(int)
     * 
     * @param lowThreshold
     *            low threshold percentage.
     * @return this.
     */
	public PhysicalMemoryUtilizationAlertConfigurer resolveAlertIfMemoryBelow(int lowThreshold) {
		config.setLowThresholdPerc(lowThreshold);
		return this;
	}

    /**
     * Set the period of time a physical memory alert should be triggered if it's reading is above/below the
     * threshold setting.
     * 
     * @param period
     *            period of time.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
	public PhysicalMemoryUtilizationAlertConfigurer measurementPeriod(long period, TimeUnit timeUnit) {
		config.setMeasurementPeriod(period, timeUnit);
		return this;
	}
	
	/**
	 * Get a fully configured physical memory utilization configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	@Override
    public PhysicalMemoryUtilizationAlertConfiguration create() {
		return config;
	}
}
