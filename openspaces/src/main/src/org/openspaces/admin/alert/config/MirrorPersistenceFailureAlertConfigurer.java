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
 * A Mirror persistence failure alert configurer. The alert is raised if the Mirror failed to
 * persist to the DB. The alert is resolved when the Mirror manages to persist to the DB for the
 * first time after the alert has been triggered.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link MirrorPersistenceFailureAlertConfiguration} configuration.
 * 
 * @see MirrorPersistenceFailureAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class MirrorPersistenceFailureAlertConfigurer implements AlertConfigurer {

	private final MirrorPersistenceFailureAlertConfiguration config = new MirrorPersistenceFailureAlertConfiguration();
	
	/**
	 * Constructs an empty replication channel disconnection alert configuration.
	 */
	public MirrorPersistenceFailureAlertConfigurer() {
	}

	/**
	 * Get a fully configured replication channel disconnection configuration (after all properties have been set).
	 * @return a fully configured alert bean configuration.
	 */
	public MirrorPersistenceFailureAlertConfiguration create() {
		return config;
	}
}
