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

    public AbstractAlert(Alert alert) {
        this.alert = alert;
    }
    
    /** The alert set upon construction */
    public Alert getAlert() {
        return alert;
    }

    public String getAlertUid() {
        return alert.getAlertUid();
    }

    public String getComponentUid() {
        return alert.getComponentUid();
    }

    public String getDescription() {
        return alert.getDescription();
    }

    public String getGroupUid() {
        return alert.getGroupUid();
    }

    public String getName() {
        return alert.getName();
    }

    public Map<String, String> getProperties() {
        return alert.getProperties();
    }

    public Map<String, String> getConfig() {
        return alert.getConfig();
    }

    public AlertSeverity getSeverity() {
        return alert.getSeverity();
    }

    public AlertStatus getStatus() {
        return alert.getStatus();
    }

    public long getTimestamp() {
        return alert.getTimestamp();
    }

    @Override
    public String toString() {
        return alert.toString();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        alert = (Alert)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(alert);
    }
}