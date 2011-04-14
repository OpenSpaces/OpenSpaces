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
 * A replication redo log overflow to disk alert configurer. The alert is raised if a redo log has
 * exceeded the defined memory capacity and excess packets are being written to disk. The alert is
 * resolved once the disk is no longer in use.
 * <p>
 * When target space is unavailable, replication packets are stored in the redo log (a.k.a backlog).
 * If the capacity of the memory redo log exceeds, the disk is used. Once the target reconnects, the
 * backlog is transmitted. When the disk is no longer in use, a resolution alert is triggered. On
 * the other hand, if the disk redo log capacity exceeds, then the redo log is cleared and target
 * will sync upon recovery. When the redo log is cleared the disk is no longer in use and an alert
 * will be triggered as well.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ReplicationRedoLogOverflowToDiskAlertConfiguration} configuration.
 * 
 * @see ReplicationRedoLogOverflowToDiskAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogOverflowToDiskAlertConfigurer implements AlertConfigurer {

	private final ReplicationRedoLogOverflowToDiskAlertConfiguration config = new ReplicationRedoLogOverflowToDiskAlertConfiguration();
	
	/**
	 * Constructs an empty alert configuration.
	 */
	public ReplicationRedoLogOverflowToDiskAlertConfigurer() {
	}
	
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    public ReplicationRedoLogOverflowToDiskAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }

	/**
	 * Get a fully configured configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public ReplicationRedoLogOverflowToDiskAlertConfiguration create() {
		return config;
	}
}
