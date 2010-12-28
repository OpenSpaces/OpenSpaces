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

/**
 * A strongly typed alert bean configurer triggered when a replication redo log size crosses a
 * certain threshold.
 * <p>
 * Allows a more code-fluent approach by use of method chaining. After all properties have been set,
 * use the call to {@link #getConfig()} to create a fully initialized configuration object based.
 * 
 * @see ReplicationRedoLogSizeAlertBeanConfig
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogSizeAlertBeanConfigurer implements AlertBeanConfigurer {

	private final ReplicationRedoLogSizeAlertBeanConfig config = new ReplicationRedoLogSizeAlertBeanConfig();
	
	/**
	 * Constructs an empty alert configuration.
	 */
	public ReplicationRedoLogSizeAlertBeanConfigurer() {
	}
	
	   /**
     * Set the high threshold redo-log size value.
     * @param highThreshold high threshold redo-log size.
     * @return this.
     */
    public ReplicationRedoLogSizeAlertBeanConfigurer highThresholdRedoLogSize(int highThreshold) {
        config.setHighThresholdRedoLogSize(highThreshold);
        return this;
    }
    
    /**
     * Set the low threshold redo-log size value.
     * @param lowThreshold low threshold size.
     * @return this.
     */
    public ReplicationRedoLogSizeAlertBeanConfigurer lowThresholdRedoLogSize(int lowThreshold) {
        config.setLowThresholdRedoLogSize(lowThreshold);
        return this;
    }

	/**
	 * Get a fully configured configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public ReplicationRedoLogSizeAlertBeanConfig getConfig() {
		return config;
	}
}
