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
 * A strongly typed replication channel disconnection alert bean configurer. Allows a more code-fluent
 * approach by use of method chaining. After all properties have been set, use the call to
 * {@link #getConfig()} to create a fully initialized configuration object based.
 * 
 * @see ReplicationChannelDisconnectedAlertBeanConfig
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationChannelDisconnectedAlertBeanConfigurer implements AlertBeanConfigurer {

	private final ReplicationChannelDisconnectedAlertBeanConfig config = new ReplicationChannelDisconnectedAlertBeanConfig();
	
	/**
	 * Constructs an empty replication channel disconnection alert configuration.
	 */
	public ReplicationChannelDisconnectedAlertBeanConfigurer() {
	}

	/**
	 * Get a fully configured replication channel disconnection configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public ReplicationChannelDisconnectedAlertBeanConfig getConfig() {
		return config;
	}
}
