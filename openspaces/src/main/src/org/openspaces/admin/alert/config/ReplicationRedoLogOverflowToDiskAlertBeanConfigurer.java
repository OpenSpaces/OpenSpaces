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
 * A strongly typed alert bean configurer triggered when a replication redo log has exceeded the
 * redo-log memory capacity, and has been swapped to disk.
 * <p>
 * Allows a more code-fluent approach by use of method chaining. After all properties have been set,
 * use the call to {@link #getConfig()} to create a fully initialized configuration object based.
 * 
 * @see ReplicationRedoLogOverflowToDiskAlertBeanConfig
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogOverflowToDiskAlertBeanConfigurer implements AlertBeanConfigurer {

	private final ReplicationRedoLogOverflowToDiskAlertBeanConfig config = new ReplicationRedoLogOverflowToDiskAlertBeanConfig();
	
	/**
	 * Constructs an empty alert configuration.
	 */
	public ReplicationRedoLogOverflowToDiskAlertBeanConfigurer() {
	}

	/**
	 * Get a fully configured configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public ReplicationRedoLogOverflowToDiskAlertBeanConfig getConfig() {
		return config;
	}
}
