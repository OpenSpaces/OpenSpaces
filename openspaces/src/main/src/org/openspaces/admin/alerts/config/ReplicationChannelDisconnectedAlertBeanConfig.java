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

import java.util.Map;

import org.openspaces.admin.internal.alerts.bean.ReplicationChannelDisconnectedAlertBean;
import org.openspaces.core.util.StringProperties;

/**
 * A strongly typed replication channel disconnection alert bean configuration.
 * 
 * @see ReplicationChannelDisconnectedAlertBeanConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationChannelDisconnectedAlertBeanConfig implements AlertBeanConfig {
    private static final long serialVersionUID = 1L;

	private final StringProperties properties = new StringProperties();

	/**
	 * Constructs an empty replication channel disconnection configuration.
	 */
	public ReplicationChannelDisconnectedAlertBeanConfig() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProperties(Map<String, String> properties) {
	    this.properties.clear();
	    this.properties.putAll(properties);
	}

	/**
     * {@inheritDoc}
     */
	public Map<String, String> getProperties() {
		return properties.getProperties();
	}

	/**
     * {@inheritDoc}
     */
	public String getBeanClassName() {
		return ReplicationChannelDisconnectedAlertBean.class.getName();
	}
}
