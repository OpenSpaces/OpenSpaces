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

import org.openspaces.admin.internal.alert.bean.ReplicationRedoLogSizeAlertBean;

/**
 * A replication redo log size alert configuration. Specifies the thresholds for triggering an alert.
 * There are two thresholds, high and low. The redo log size alert is raised if the number of
 * packets in the redo log is above the specified high threshold. The redo log size alert is
 * resolved if the number of packets in the redo log goes below the specified low threshold.
 * 
 * @see ReplicationRedoLogSizeAlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogSizeAlertConfiguration implements AlertConfiguration {
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
    
    
	private final Map<String, String> properties = new HashMap<String, String>();

    private boolean enabled;

	/**
	 * Constructs an empty configuration.
	 */
	public ReplicationRedoLogSizeAlertConfiguration() {
	}
	
	/**
     * Set the high threshold redo-log size value - the number of packets in the redo log.
     * 
     * @param highThreshold high threshold redo-log size.
     */
    public void setHighThresholdRedoLogSize(int highThreshold) {
        properties.put(HIGH_THRESHOLD_REDO_LOG_SIZE_KEY, String.valueOf(highThreshold));
    }
    
    /**
     * @return the high threshold redo-log size - the number of packets in the redo log.
     */
    public Integer getHighThresholdRedoLogSize() {
        String value = properties.get(HIGH_THRESHOLD_REDO_LOG_SIZE_KEY);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * Set the low threshold redo-log size value.
     * @param lowThreshold low threshold redo-log size.
     */
    public void setLowThresholdRedoLogSize(int lowThreshold) {
        properties.put(LOW_THRESHOLD_REDO_LOG_SIZE_KEY, String.valueOf(lowThreshold));
    }
    
    /**
     * @return the low threshold redo-log size
     */
    public Integer getLowThresholdRedoLogSize() {
        String value = properties.get(LOW_THRESHOLD_REDO_LOG_SIZE_KEY);
        if (value == null) return null;
        return Integer.valueOf(value);
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
		return ReplicationRedoLogSizeAlertBean.class.getName();
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
