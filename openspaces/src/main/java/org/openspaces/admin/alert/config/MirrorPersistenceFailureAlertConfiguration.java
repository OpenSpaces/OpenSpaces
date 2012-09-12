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

import org.openspaces.admin.internal.alert.bean.MirrorPersistenceFailureAlertBean;

/**
 * A Mirror persistence failure alert configuration. The alert is raised if the Mirror failed to
 * persist to the DB. The alert is resolved when the Mirror manages to persist to the DB for the
 * first time after the alert has been triggered.
 * 
 * @see MirrorPersistenceFailureAlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class MirrorPersistenceFailureAlertConfiguration implements AlertConfiguration {
    private static final long serialVersionUID = 1L;  
    
	private final Map<String, String> properties = new HashMap<String, String>(0);

    private boolean enabled;

	/**
	 * Constructs an empty configuration.
	 */
	public MirrorPersistenceFailureAlertConfiguration() {
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
		return MirrorPersistenceFailureAlertBean.class.getName();
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
