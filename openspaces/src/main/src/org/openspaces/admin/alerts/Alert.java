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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.Admin;

/**
 * A plain java object representing an alert issued by an alert bean or an alert provider.
 * <p>
 * An alert has a unique alert id ({@link #getAlertId()}) and is issued by one of the alert beans,
 * classified under a type ({@link #getAlertType()}). Each alert has descriptive information of the
 * alert ({@link #getAlertDescription()}), and has access to all the configuration properties and
 * any runtime properties the alert bean exposes (see {@link #getProperties()}).
 * <p>
 * The source component for which the alert was issued (see {@link #getSourceComponentUid()}) can be
 * correlated to one of the components using the {@link Admin#getGridComponentByUID(String)} if this
 * component has not yet been terminated.
 * <p>
 * A <B>negative</B> alert is an alert that indicates a problematic situation that needs attention.
 * An alert bean can trigger more than one negative alert if the problem persists (for the same
 * alert type). A <B>positive</B> alert is an alert that indicates that the situation was resolved,
 * or is no longer in need of attention.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class Alert implements Serializable {
	
    private static final long serialVersionUID = 1L;
    private String alertId;
	private String alertType;
	private String sourceComponentUid;
	private String alertDescription;
	private boolean isPositive;
	private Map<String, String> properties;
	
	/**
	 * Empty construction of an alert instance.
	 */
	public Alert() {
	}
	
	/**
	 * @return A unique alert identification string.
	 */
	public String getAlertId() {
		return alertId;
	}
	
	/**
	 * Set a unique alert identification string. Optional.
	 * @param alertId the alert Id.
	 */
	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}
	
	/**
	 * @return an alert type (classification).
	 */
	public String getAlertType() {
		return alertType;
	}
	
	/**
	 * The type of the alert which is a meaningful string.
	 * @param alertType the alert type.
	 */
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	
	/**
	 * @return the source component UID for which this alert was issued. Can be null.
	 */
	public String getSourceComponentUid() {
		return sourceComponentUid;
	}
	
	/**
	 * A unique ID of the component that triggered the alert. In case of a grid wide alert,
	 * the source component can be null.
	 * 
	 * @param sourceComponentUid the source component UID.
	 */
	public void setSourceComponentUid(String sourceComponentUid) {
		this.sourceComponentUid = sourceComponentUid;
	}
	
    /**
     * @return <code>false</code> if the alert is in need of attention; <code>true</code> if no
     *         longer in need of attention.
     */
	public boolean isPositive() {
		return isPositive;
	}

    /**
     * An indication if the alert is in need of attention (negative) or not (positive indication
     * about previously detected negative condition).
     * 
     * @param isPositive <code>false</code> if in need of attention; <code>false</code> otherwise. 
     */
	public void setPositive(boolean isPositive) {
		this.isPositive = isPositive;
	}

    /**
     * @return  A map of String key-value property pairs including configuration properties, and any runtime
     * properties exposed by the alert bean.
     */
	public Map<String, String> getProperties() {
		return properties;
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
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

    /**
     * A convenience method for adding properties to an already existing set of properties.
     */
	public void putAllProperties(Map<String, String> properties) {
	    if (this.properties == null) {
	        this.properties = new HashMap<String, String>();
	    }
        this.properties.putAll(properties);
    }
	
	/**
	 * A convenience method for adding a single property to an already existing set of properties.
	 */
	public void putProperty(String key, String value) {
		if (this.properties == null) {
			this.properties = new HashMap<String, String>();
		}
		this.properties.put(key, value);
	}
	
	/**
	 * A more elaborated description of the alert. 
	 * @param alertDescription an alert description.
	 */
	public void setAlertDescription(String alertDescription) {
		this.alertDescription = alertDescription;
	}
	
	/**
	 * @return an alert description.
	 */
	public String getAlertDescription() {
		return alertDescription;
	}
	
	@Override
	public String toString() {
	    return getAlertId()+" " + (isPositive()? "[+]":"[-]") + " " + getAlertDescription();
	}
}
