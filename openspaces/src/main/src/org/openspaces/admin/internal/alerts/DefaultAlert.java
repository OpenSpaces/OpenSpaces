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
package org.openspaces.admin.internal.alerts;

import java.util.Date;
import java.util.Map;

import org.openspaces.admin.alerts.AlertFactory;
import org.openspaces.admin.alerts.AlertSeverity;
import org.openspaces.admin.alerts.AlertStatus;

/**
 * A plain java object representing an alert issued by an alert bean or an alert provider.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class DefaultAlert implements InternalAlert {
	
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String description;
    private long timestamp;
    private AlertSeverity severity;
    private AlertStatus status;
    private String beanConfigClassName;
    private String alertUid;
    private String groupUid;
    private String componentUid;
	private Map<String, String> properties;

	/**
	 * @see AlertFactory
	 */
	public DefaultAlert() {
	}
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getName()
     */
	public String getName() {
        return name;
    }
	
	public void setName(String name) {
        this.name = name;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getDescription()
     */
	public String getDescription() {
        return description;
    }
	
	public void setDescription(String description) {
        this.description = description;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getTimestamp()
     */
	public long getTimestamp() {
        return timestamp;
    }
	
	public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getSeverity()
     */
	public AlertSeverity getSeverity() {
        return severity;
    }
	
	public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }
	
	/*
     * (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getStatus()
     */
    public AlertStatus getStatus() {
        return status;
    }
    
    public void setStatus(AlertStatus status) {
        this.status = status;
    }

	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getBeanConfigClassName()
     */
	public String getBeanConfigClassName() {
        return beanConfigClassName;
    }
	
	public void setBeanConfigClassName(String beanConfigClassName) {
        this.beanConfigClassName = beanConfigClassName;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getAlertUid()
     */
	public String getAlertUid() {
        return alertUid;
    }
	
	public void setAlertUid(String alertUid) {
        this.alertUid = alertUid;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getGroupUid()
     */
	public String getGroupUid() {
        return groupUid;
    }
	
	public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getComponentUid()
     */
	public String getComponentUid() {
        return componentUid;
    }
	
	public void setComponentUid(String componentUid) {
        this.componentUid = componentUid;
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.alerts.Alert#getProperties()
     */
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

	@Override
	public String toString() {
	    return new Date(getTimestamp()) + " | " + getStatus() + " | " + getSeverity()+" | " + getName() + " |" + getDescription() + " | " + getProperties();
	}
}
