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
package org.openspaces.admin.alerts;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.alerts.DefaultAlert;

/**
 * A factory for constructing an alert instance to be fired by an alert bean.
 * <p>
 * <li> {@link #groupUid(String)} - <b>required</b> </li>
 * <li> {@link #severity(AlertSeverity)} - <b>required</b> </li>
 * <li> {@link #beanClassName(String)} - <b>required</b> </li>
 * <li> {@link #timestamp(long)} - optional, is set upon construction</li>
 * <li> {@link #name(String)} - optional (<code>null</code> by default)</li>
 * <li> {@link #description(String)} - optional (<code>null</code> by default)</li>
 * <li> {@link #componentUid(String)} - optional (<code>null</code> by default)</li>
 * <li> {@link #properties(Map)} - optional (<code>null</code> by default)</li>
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertFactory {

    private final DefaultAlert alert = new DefaultAlert();
    
    /**
     * A factory for creating an {@link Alert}.
     * <p>
     * <li> {@link #groupUid(String)} - <b>required</b> </li>
     * <li> {@link #severity(AlertSeverity)} - <b>required</b> </li>
     * <li> {@link #beanClassName(String)} - <b>required</b> </li>
     * <li> {@link #timestamp(long)} - optional, is set upon construction</li>
     * <li> {@link #name(String)} - optional (<code>null</code> by default)</li>
     * <li> {@link #description(String)} - optional (<code>null</code> by default)</li>
     * <li> {@link #componentUid(String)} - optional (<code>null</code> by default)</li>
     * <li> {@link #properties(Map)} - optional (<code>null</code> by default)</li>
     */
    public AlertFactory() {
        alert.setTimestamp(System.currentTimeMillis());
    }
    
    /** Alert name - the name assigned to the alert. */
    public AlertFactory name(String name) {
        alert.setName(name);
        return this;
    }
    
    /** Description - a description of the alert. */
    public AlertFactory description(String description) {
        alert.setDescription(description);
        return this;
    }

    /**
     * Timestamp - the date and time the alert occurred. Timestamp is already set by the factory
     * upon construction. */
    public AlertFactory timestamp(long timestamp) {
        alert.setTimestamp(timestamp);
        return this;
    }
    
    /** Severity - the defined severity of the alert. */
    public AlertFactory severity(AlertSeverity severity) {
        alert.setSeverity(severity);
        return this;
    }
    
    /** Bean Class Name - the alert bean class name which generated the alert. */
    public AlertFactory beanClassName(String beanClassName) {
        alert.setBeanClassName(beanClassName);
        return this;
    }

    /**
     * A group unique identifier representing the aggregation of alerts belonging to the same alert
     * bean, for a specific component. The group contains the history of alerts from the first
     * unresolved alert triggered until the alert is resolved.
     */
    public AlertFactory groupUid(String groupUid) {
        alert.setGroupUid(groupUid);
        return this;
    }
    
    /** Component UID - the unique identifier of the component associated with the alert. */
    public AlertFactory componentUid(String componentUid) {
        alert.setComponentUid(componentUid);
        return this;
    }

    /**
     * Set of String key-value property pairs including configuration properties, and any runtime
     * properties exposed by the alert bean. It is useful to put CPU, Memory and GC metrics into the
     * map as a basic set of attributes for ease of troubleshooting. <b>Overrides any previously set
     * properties.</b>
     * 
     * @param properties
     *            the properties of an alert bean.
     */
    public AlertFactory properties(Map<String, String> properties) {
        alert.setProperties(properties);
        return this;
    }

    /**
     * A convenience method for adding properties to an already existing set of properties.
     */
    public AlertFactory putAllProperties(Map<String, String> properties) {
        if (alert.getProperties() == null) {
            alert.setProperties(properties);
        } else {
            alert.getProperties().putAll(properties);
        }
        return this;
    }
    
    /**
     * A convenience method for adding a single property to an already existing set of properties.
     */
    public AlertFactory putProperty(String key, String value) {
        if (alert.getProperties() == null) {
            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put(key, value);
            alert.setProperties(properties);
        } else {
            alert.getProperties().put(key, value);
        }
        return this;
    }
    
    /**
     * @return the constructed alert
     * @throws AdminException if required configurations were not set properly ({@link #groupUid(String), #severity(AlertSeverity), #beanClassName(String))
     */
    public Alert toAlert() {
        if (alert.getGroupUid() == null) {
            throw new AdminException("Alert should be configured with a unique group id");
        }
        if (alert.getSeverity() == null) {
            throw new AdminException("Alert should be configured with a severity level");
        }
        if (alert.getBeanClassName() == null) {
            throw new AdminException("Alert should be configured with a bean class name which fires the alert");
        }
        return alert;
    }
    
}
