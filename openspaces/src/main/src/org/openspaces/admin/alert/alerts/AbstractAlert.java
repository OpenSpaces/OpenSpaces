/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.alert.alerts;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;

/**
 * An abstraction over a fired alert, exposing the {@link Alert} API. Subclass to introduce a new
 * type of alert, with strongly typed getter methods over runtime properties specified by
 * {@link Alert#getProperties()}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class AbstractAlert implements Alert {

    private static final long serialVersionUID = 1L;
    private Alert alert;

    /** required by java.io.Externalizable */
    public AbstractAlert() {
    }
    
    public AbstractAlert(Alert alert) {
        this.alert = alert;
    }
    
    /** The alert set upon construction */
    public Alert getAlert() {
        return alert;
    }

    @Override
    public String getAlertUid() {
        return alert.getAlertUid();
    }

    @Override
    public String getComponentUid() {
        return alert.getComponentUid();
    }
    
    @Override
    public String getComponentDescription() {
        return alert.getComponentDescription();
    }

    @Override
    public String getDescription() {
        return alert.getDescription();
    }

    @Override
    public String getGroupUid() {
        return alert.getGroupUid();
    }

    @Override
    public String getName() {
        return alert.getName();
    }

    @Override
    public Map<String, String> getProperties() {
        return alert.getProperties();
    }

    @Override
    public Map<String, String> getConfig() {
        return alert.getConfig();
    }

    @Override
    public AlertSeverity getSeverity() {
        return alert.getSeverity();
    }

    @Override
    public AlertStatus getStatus() {
        return alert.getStatus();
    }

    @Override
    public long getTimestamp() {
        return alert.getTimestamp();
    }

    @Override
    public String toString() {
        return alert.toString();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        alert = (Alert)in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(alert);
    }
}
