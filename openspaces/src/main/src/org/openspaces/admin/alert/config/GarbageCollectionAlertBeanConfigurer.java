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
 * A strongly typed long garbage collection pause alert bean configurer. Allows a more code-fluent
 * approach by use of method chaining. After all properties have been set, use the call to
 * {@link #getConfig()} to create a fully initialized configuration object based.
 * 
 * @see GarbageCollectionAlertBeanConfig
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class GarbageCollectionAlertBeanConfigurer implements AlertBeanConfigurer {

    private final GarbageCollectionAlertBeanConfig config = new GarbageCollectionAlertBeanConfig();

    /**
     * Constructs an empty garbage collection pause alert configuration.
     */
    public GarbageCollectionAlertBeanConfigurer() {
    }

    /**
     * Set the period of time a long GC pause alert should be raised for.
     * 
     * @param period
     *            period of time spent on GC.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
    public GarbageCollectionAlertBeanConfigurer raiseAlertForGcDurationOf(long period, TimeUnit timeUnit) {
        config.setLongGcPausePeriod(period, timeUnit);
        return this;
    }
    
    /**
     * Set the period of time a GC pause alert should be resolved for.
     * 
     * @param period
     *            period of time spent on GC.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
    public GarbageCollectionAlertBeanConfigurer resolveAlertForGcDurationOf(long period, TimeUnit timeUnit) {
        config.setShortGcPausePeriod(period, timeUnit);
        return this;
    }

    /**
     * Get a fully configured garbage collection pause configuration (after all properties have been
     * set).
     * 
     * @return a fully configured alert bean configuration.
     */
    public GarbageCollectionAlertBeanConfig getConfig() {
        return config;
    }
}
