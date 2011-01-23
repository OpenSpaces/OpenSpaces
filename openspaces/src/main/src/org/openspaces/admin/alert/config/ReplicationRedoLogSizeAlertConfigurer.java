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

/**
 * A replication redo log size alert configurer. Specifies the thresholds for triggering an alert.
 * There are two thresholds, high and low. The redo log size alert is raised if the number of
 * packets in the redo log is above the specified high threshold. The redo log size alert is
 * resolved if the number of packets in the redo log goes below the specified low threshold.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ReplicationRedoLogSizeAlertConfiguration} configuration.
 * 
 * @see ReplicationRedoLogSizeAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogSizeAlertConfigurer implements AlertConfigurer {

	private final ReplicationRedoLogSizeAlertConfiguration config = new ReplicationRedoLogSizeAlertConfiguration();
	
	/**
	 * Constructs an empty alert configuration.
	 */
	public ReplicationRedoLogSizeAlertConfigurer() {
	}
	
	/**
	 * Raise an alert if the number of packets in the redo log is above the specified threshold.
	 * @see ReplicationRedoLogSizeAlertConfiguration#setHighThresholdRedoLogSize(int)
	 * 
     * @param highThreshold high threshold redo-log size.
     * @return this.
     */
    public ReplicationRedoLogSizeAlertConfigurer raiseAlertIfRedoLogSizeAbove(int highThreshold) {
        config.setHighThresholdRedoLogSize(highThreshold);
        return this;
    }
    
    /**
     * Resolve a previously raised alert if the number of packets in the redo log goes below the specified threshold.
     * @see ReplicationRedoLogSizeAlertConfiguration#setLowThresholdRedoLogSize(int)
     * 
     * @param lowThreshold low threshold size.
     * @return this.
     */
    public ReplicationRedoLogSizeAlertConfigurer resolveAlertIfRedoLogSizeBelow(int lowThreshold) {
        config.setLowThresholdRedoLogSize(lowThreshold);
        return this;
    }

	/**
	 * Get a fully configured configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public ReplicationRedoLogSizeAlertConfiguration create() {
		return config;
	}
}
