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
 * A heap utilization alert configurer. Specifies the thresholds for triggering an alert. There are
 * two thresholds, high and low and a measurement period indicating a window for the heap reading.
 * The heap utilization alert is raised if any discovered JVM is above the specified heap threshold
 * for a period of time. The heap utilization alert is resolved if its heap goes below the specified
 * heap threshold for a period of time.
 * <p>
 * Use the call to {@link #getConfig()} to create a fully initialized
 * {@link HeapMemoryUtilizationAlertConfiguration} configuration.
 * 
 * @see HeapMemoryUtilizationAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class HeapMemoryUtilizationAlertConfigurer implements AlertConfigurer {

    private final HeapMemoryUtilizationAlertConfiguration config = new HeapMemoryUtilizationAlertConfiguration();

    /**
     * Constructs an empty heap memory utilization alert configuration.
     */
    public HeapMemoryUtilizationAlertConfigurer() {
    }

    /**
     * Raise an alert if heap utilization if above the specified threshold for a period of time. The
     * period of time is configured using {@link #measurementPeriod(long, TimeUnit)}.
     * 
     * @see HeapMemoryUtilizationAlertConfiguration#setHighThresholdPerc(int)
     * 
     * @param highThreshold
     *            high threshold percentage.
     * @return this.
     */
    public HeapMemoryUtilizationAlertConfigurer raiseAlertIfHeapAboveThreshold(int highThreshold) {
        config.setHighThresholdPerc(highThreshold);
        return this;
    }

    /**
     * Resolve the previously raised alert if heap utilization goes below the specified threshold
     * for a period of time. The period of time is configured using
     * {@link #measurementPeriod(long, TimeUnit)}.
     * 
     * @see HeapMemoryUtilizationAlertConfiguration#setLowThresholdPerc(int)
     * 
     * @param lowThreshold
     *            low threshold percentage.
     * @return this.
     */
    public HeapMemoryUtilizationAlertConfigurer resolveAlertIfHeapBelowThreshold(int lowThreshold) {
        config.setLowThresholdPerc(lowThreshold);
        return this;
    }

    /**
     * Set the period of time a heap memory alert should be triggered if it's reading is above/below
     * the threshold setting.
     * 
     * @param period
     *            period of time.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
    public HeapMemoryUtilizationAlertConfigurer measurementPeriod(long period, TimeUnit timeUnit) {
        config.setMeasurementPeriod(period, timeUnit);
        return this;
    }

    /**
     * Get a fully configured heap memory utilization configuration (after all properties have been
     * set).
     * 
     * @return a fully configured alert bean configuration.
     */
    public HeapMemoryUtilizationAlertConfiguration getConfig() {
        return config;
    }
}
