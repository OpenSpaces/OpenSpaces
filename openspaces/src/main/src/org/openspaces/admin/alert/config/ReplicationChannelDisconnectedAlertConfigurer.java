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
 * A replication channel disconnection alert configurer. The alert is raised if a replication
 * channel connection between a source (primary) and it's target (backup/mirror) has been
 * disconnected. The alert is resolved once the connection is re-established.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link ReplicationChannelDisconnectedAlertConfiguration} configuration.
 * 
 * @see ReplicationChannelDisconnectedAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationChannelDisconnectedAlertConfigurer implements AlertConfigurer {

	private final ReplicationChannelDisconnectedAlertConfiguration config = new ReplicationChannelDisconnectedAlertConfiguration();
	
	/**
	 * Constructs an empty replication channel disconnection alert configuration.
	 */
	public ReplicationChannelDisconnectedAlertConfigurer() {
	}
	
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    public ReplicationChannelDisconnectedAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }

	/**
	 * Get a fully configured replication channel disconnection configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public ReplicationChannelDisconnectedAlertConfiguration create() {
		return config;
	}
}
