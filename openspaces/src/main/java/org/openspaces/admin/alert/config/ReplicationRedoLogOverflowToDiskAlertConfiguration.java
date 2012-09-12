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

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.alert.bean.ReplicationRedoLogOverflowToDiskAlertBean;

/**
 * A replication redo log overflow to disk alert configuration. The alert is raised if a redo log has
 * exceeded the defined memory capacity and excess packets are being written to disk. The alert is
 * resolved once the disk is no longer in use.
 * <p>
 * When target space is unavailable, replication packets are stored in the redo log (a.k.a backlog).
 * If the capacity of the memory redo log exceeds, the disk is used. Once the target reconnects, the
 * backlog is transmitted. When the disk is no longer in use, a resolution alert is triggered. On
 * the other hand, if the disk redo log capacity exceeds, then the redo log is cleared and target
 * will sync upon recovery. When the redo log is cleared the disk is no longer in use and an alert
 * will be triggered as well.
 * 
 * @see ReplicationRedoLogOverflowToDiskAlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogOverflowToDiskAlertConfiguration implements AlertConfiguration {
    private static final long serialVersionUID = 1L;

	private final Map<String, String> properties = new HashMap<String, String>();

    private boolean enabled;

	/**
	 * Constructs an empty configuration.
	 */
	public ReplicationRedoLogOverflowToDiskAlertConfiguration() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void setProperties(Map<String, String> properties) {
	    this.properties.clear();
	    this.properties.putAll(properties);
	}

	/**
     * {@inheritDoc}
     */
	@Override
    public Map<String, String> getProperties() {
		return properties;
	}

	/**
     * {@inheritDoc}
     */
	@Override
    public String getBeanClassName() {
		return ReplicationRedoLogOverflowToDiskAlertBean.class.getName();
	}
	   
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
