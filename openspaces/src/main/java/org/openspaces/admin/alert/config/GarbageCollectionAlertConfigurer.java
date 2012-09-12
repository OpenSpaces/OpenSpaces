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
 * A Garbage Collection pause alert configurer. Specifies the thresholds for triggering an alert. There are
 * two thresholds, long period and short period indicating how long a gc took. The garbage collection alert
 * is raised if the gc took longer than the specified 'long' period. The garbage collection alert is resolved
 * if gc took less than the specified 'short' period.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link GarbageCollectionAlertConfiguration} configuration.
 * 
 * @see GarbageCollectionAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class GarbageCollectionAlertConfigurer implements AlertConfigurer {

    private final GarbageCollectionAlertConfiguration config = new GarbageCollectionAlertConfiguration();

    /**
     * Constructs an empty garbage collection pause alert configuration.
     */
    public GarbageCollectionAlertConfigurer() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    @Override
    public GarbageCollectionAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }

    /**
     * Raise an alert if gc took longer than the specified period of time.
     * @see GarbageCollectionAlertConfiguration#setLongGcPausePeriod(long, TimeUnit)
     * 
     * @param period
     *            period of time spent on GC.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
    public GarbageCollectionAlertConfigurer raiseAlertForGcDurationOf(long period, TimeUnit timeUnit) {
        config.setLongGcPausePeriod(period, timeUnit);
        return this;
    }
    
    /**
     * Resolve a previously raised alert if gc took less than the specified period of time.
     * @see GarbageCollectionAlertConfiguration#setShortGcPausePeriod(long, TimeUnit)
     * 
     * @param period
     *            period of time spent on GC.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
    public GarbageCollectionAlertConfigurer resolveAlertForGcDurationOf(long period, TimeUnit timeUnit) {
        config.setShortGcPausePeriod(period, timeUnit);
        return this;
    }

    /**
     * Get a fully configured garbage collection pause configuration (after all properties have been
     * set).
     * 
     * @return a fully configured alert bean configuration.
     */
    @Override
    public GarbageCollectionAlertConfiguration create() {
        return config;
    }
}
