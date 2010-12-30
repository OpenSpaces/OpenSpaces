package org.openspaces.admin.alert.alerts;

import java.util.Map;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;

public class AbstractAlert implements Alert {

    private static final long serialVersionUID = 1L;
    protected final Alert alert;

    public AbstractAlert(Alert alert) {
        this.alert = alert;
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

}