package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.PhysicalMemoryUtilizationAlertBeanConfigurer;
import org.openspaces.admin.alert.events.AlertEventListener;

/**
 * A physical memory utilization alert, fired upon triggered machine physical memory thresholds. The
 * alert is raised when memory crosses a 'high' threshold for a specified period of time. The alert
 * is resolved when memory crosses a 'low' threshold for a specified period of time.
 * <p>
 * These thresholds can be configured by using the {@link PhysicalMemoryUtilizationAlertBeanConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertEventListener#onAlert(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class PhysicalMemoryUtilizationAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String MEMORY_UTILIZATION = "memory-utilization";
    
    public PhysicalMemoryUtilizationAlert(Alert alert) {
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
    
    /**
     * The memory utilization reading when this alert was fired.
     * @return the memory utilization; may be <code>null</code>.
     */
    public Double getMemoryUtilization() {
        String value = getProperties().get(MEMORY_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
}
