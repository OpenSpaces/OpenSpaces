package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.CpuUtilizationAlertBeanConfigurer;
import org.openspaces.admin.alert.events.AlertEventListener;

/**
 * A CPU Utilization alert fired upon triggered CPU thresholds. The alert is raised when CPU crosses
 * a 'high' threshold for a specified period of time. The alert is resolved when CPU crosses a 'low'
 * threshold for a specified period of time.
 * <p>
 * These thresholds can be configured by using the {@link CpuUtilizationAlertBeanConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertEventListener#onAlert(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class CpuUtilizationAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    
    public CpuUtilizationAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * The host address of the machine that this alert corresponds to.
     * @return the host address; may be <code>null</code>.
     */
    public String getHostAddress() {
        return getProperties().get(HOST_ADDRESS);
    }
    
    /**
     * The host name of the machine that this alert corresponds to.
     * @return the host name; may be <code>null</code>.
     */
    public String getHostName() {
        return getProperties().get(HOST_NAME);
    }
    
    /**
     * The CPU utilization reading when this alert was fired.
     * @return the CPU utilization; may be <code>null</code>.
     */
    public Double getCpuUtilization() {
        String value = getProperties().get(CPU_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
}
