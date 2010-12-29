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

import org.openspaces.admin.internal.alerts.bean.ReplicationRedoLogSizeAlertBean;
import org.openspaces.core.util.StringProperties;

/**
 * A strongly typed alert bean configuration triggered when a replication redo log size crosses a
 * certain threshold. The redo-log size takes both the swapped packets and the memory residing
 * packets into consideration.
 * 
 * @see ReplicationRedoLogSizeAlertBeanConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogSizeAlertBeanConfig implements AlertBeanConfig {
    private static final long serialVersionUID = 1L;

    /**
     * High threshold for redo-log size. Raises an alert if redo-log size (of both swapped packets
     * and memory residing packets) goes above this high threshold.
     */
    public static final String HIGH_THRESHOLD_REDO_LOG_SIZE_KEY = "high-threshold-redo-log-size";

    /**
     * Low threshold for redo-log size. Resolves a previously high threshold triggered alert. A
     * 'resolved' alert will be triggered if redo-log size goes below this low threshold.
     */
    public static final String LOW_THRESHOLD_REDO_LOG_SIZE_KEY = "low-threshold-redo-log-size";
    
    
	private final StringProperties properties = new StringProperties();

	/**
	 * Constructs an empty configuration.
	 */
	public ReplicationRedoLogSizeAlertBeanConfig() {
	}
	
	/**
     * Set the high threshold redo-log size value.
     * @param highThreshold high threshold redo-log size.
     */
    public void setHighThresholdRedoLogSize(int highThreshold) {
        properties.putInteger(HIGH_THRESHOLD_REDO_LOG_SIZE_KEY, highThreshold);
    }
    
    /**
     * @return the high threshold redo-log size
     */
    public Integer getHighThresholdRedoLogSize() {
        return Integer.valueOf(properties.get(HIGH_THRESHOLD_REDO_LOG_SIZE_KEY)).intValue();
    }
    
    /**
     * Set the low threshold redo-log size value.
     * @param lowThreshold low threshold redo-log size.
     */
    public void setLowThresholdRedoLogSize(int lowThreshold) {
        properties.putInteger(LOW_THRESHOLD_REDO_LOG_SIZE_KEY, lowThreshold);
    }
    
    /**
     * @return the low threshold redo-log size
     */
    public Integer getLowThresholdRedoLogSize() {
        return Integer.valueOf(properties.get(LOW_THRESHOLD_REDO_LOG_SIZE_KEY)).intValue();
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
		return ReplicationRedoLogSizeAlertBean.class.getName();
	}
}
