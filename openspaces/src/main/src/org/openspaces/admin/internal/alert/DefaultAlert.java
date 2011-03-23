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
package org.openspaces.admin.internal.alert;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;

import com.gigaspaces.internal.version.PlatformLogicalVersion;
import com.gigaspaces.lrmi.LRMIInvocationContext;

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
    private String alertUid;
    private String groupUid;
    private String componentUid;
    private String componentDescription;
    private Map<String, String> config;
	private Map<String, String> properties;


	/**
	 * @see AlertFactory
	 */
	public DefaultAlert() {
	}
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getName()
     */
	public String getName() {
        return name;
    }
	
	public void setName(String name) {
        this.name = name;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getDescription()
     */
	public String getDescription() {
        return description;
    }
	
	public void setDescription(String description) {
        this.description = description;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getTimestamp()
     */
	public long getTimestamp() {
        return timestamp;
    }
	
	public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getSeverity()
     */
	public AlertSeverity getSeverity() {
        return severity;
    }
	
	public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }
	
	/*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getStatus()
     */
    public AlertStatus getStatus() {
        return status;
    }
    
    public void setStatus(AlertStatus status) {
        this.status = status;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getAlertUid()
     */
	public String getAlertUid() {
        return alertUid;
    }
	
	public void setAlertUid(String alertUid) {
        this.alertUid = alertUid;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getGroupUid()
     */
	public String getGroupUid() {
        return groupUid;
    }
	
	public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }
	
	/* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getComponentUid()
     */
	public String getComponentUid() {
        return componentUid;
    }
	
	public void setComponentUid(String componentUid) {
        this.componentUid = componentUid;
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.openspaces.admin.alert.Alert#getComponentDescription()
	 */
	public String getComponentDescription() {
	    return componentDescription;
	}
	
	public void setComponentDescription(String componentDescription) {
        this.componentDescription = componentDescription;
    }
	
	/*
	 * @see org.openspaces.admin.alert.Alert#getConfig()
	 */
	public Map<String, String> getConfig() {
	    return config;
	}
	
	public void setConfig(Map<String,String> properties) {
	    this.config = properties;
	}

    /* (non-Javadoc)
     * @see org.openspaces.admin.alert.Alert#getProperties()
     */
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

	@Override
	public String toString() {
	    return getStatus() + " | " + getSeverity() + " | " + getName() + " |" + getDescription() + " | " + getComponentDescription() + " | " + new Date(getTimestamp());
	}
	
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        alertUid = in.readUTF();
        componentUid = in.readUTF();
        groupUid = in.readUTF();
        description = in.readUTF();
        name = in.readUTF();
        timestamp = in.readLong();
        severity = (AlertSeverity)in.readObject();
        status = (AlertStatus)in.readObject();
        config = (HashMap<String, String>)in.readObject();
        properties = (HashMap<String, String>)in.readObject();
        
        if (LRMIInvocationContext.getEndpointLogicalVersion().greaterOrEquals(PlatformLogicalVersion.v8_0_1)) {
            componentDescription = in.readUTF();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(alertUid);
        out.writeUTF(componentUid);
        out.writeUTF(groupUid);
        out.writeUTF(description);
        out.writeUTF(name);
        out.writeLong(timestamp);
        out.writeObject(severity);
        out.writeObject(status);
        out.writeObject(config);
        out.writeObject(properties);
        
        if (LRMIInvocationContext.getEndpointLogicalVersion().greaterOrEquals(PlatformLogicalVersion.v8_0_1)) {
            out.writeUTF(componentDescription);
        }
    }

}
