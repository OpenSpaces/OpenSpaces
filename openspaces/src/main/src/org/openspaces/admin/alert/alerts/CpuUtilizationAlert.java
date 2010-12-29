package org.openspaces.admin.alert.alerts;

import java.util.Map;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.events.AlertEventListener;

/**
 * An alert indicating that a CPU Utilization alert has been fired. 
 * <p>
 * This alert will be received on the call to {@link AlertEventListener#onAlert(Alert)}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class CpuUtilizationAlert implements Alert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    
    private final Alert alert;
    
    public CpuUtilizationAlert(Alert alert) {
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

    public AlertSeverity getSeverity() {
        return alert.getSeverity();
    }

    public AlertStatus getStatus() {
        return alert.getStatus();
    }

    public long getTimestamp() {
        return alert.getTimestamp();
    }

    public String getHostAddress() {
        return getProperties().get(HOST_ADDRESS);
    }
    
    public String getHostName() {
        return getProperties().get(HOST_NAME);
    }
    
    public Double getCpuUtilization() {
        String value = getProperties().get(CPU_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
}
