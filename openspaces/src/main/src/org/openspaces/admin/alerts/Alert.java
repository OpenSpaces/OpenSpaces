package org.openspaces.admin.alerts;

import java.util.HashMap;
import java.util.Map;

public class Alert {
	
	private String alertId;
	private String alertType;
	private String sourceComponentUid;
	private String alertDescription;
	private boolean isPositive;
	private Map<String, Object> properties;
	
	public Alert() {
	}
	
	public String getAlertId() {
		return alertId;
	}
	
	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}
	
	public String getAlertType() {
		return alertType;
	}
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	public String getSourceComponentUid() {
		return sourceComponentUid;
	}
	public void setSourceComponentUid(String sourceComponentUid) {
		this.sourceComponentUid = sourceComponentUid;
	}
	public boolean isPositive() {
		return isPositive;
	}
	public void setPositive(boolean isPositive) {
		this.isPositive = isPositive;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	public void setProperty(String key, Object value) {
		if (this.properties == null) {
			this.properties = new HashMap<String, Object>();
		}
		this.properties.put(key, value);
	}
	public void setAlertDescription(String alertDescription) {
		this.alertDescription = alertDescription;
	}
	public String getAlertDescription() {
		return alertDescription;
	}
	
	@Override
	public String toString() {
		return alertId;
	}
}
