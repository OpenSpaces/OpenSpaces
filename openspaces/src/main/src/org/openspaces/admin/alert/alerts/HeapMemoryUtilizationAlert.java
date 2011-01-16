package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.HeapMemoryUtilizationAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;

/**
 * A heap memory utilization alert, fired upon triggered JVM heap-memory thresholds. The alert is
 * raised when heap memory crosses a 'high' threshold for a specified period of time. The alert is
 * resolved when heap-memory crosses a 'low' threshold for a specified period of time.
 * <p>
 * These thresholds can be configured by using the {@link HeapMemoryUtilizationAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class HeapMemoryUtilizationAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    
    public static final String PROCESS_ID = "process-id";
    public static final String COMPONENT_NAME = "component-name";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    public static final String MAX_HEAP_IN_BYTES = "max-heap-in-bytes";
    
    public HeapMemoryUtilizationAlert(Alert alert) {
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
     * The process id of the component for which the alert was fired.
     * @return the process id (pid); may be <code>null</code>.
     */
    public String getProcessId() {
        return getProperties().get(PROCESS_ID);
    }

    /**
     * The name of the component for which the alert was fired (e.g. 'Grid Service Agent', 'Grid
     * Service Manager', 'Grid Service Container', 'Lookup Service').
     * 
     * @return the name of the component.
     */
    public String getComponentName() {
        return getProperties().get(COMPONENT_NAME);
    }
    
    /**
     * The Heap utilization reading when this alert was fired.
     * @return the Heap utilization; may be <code>null</code>.
     */
    public Double getHeapUtilization() {
        String value = getProperties().get(HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The maximum amount of memory in bytes that can be used for memory management. This method
     * returns -1 if the maximum memory size (-Xmx) is undefined.
     * 
     * @return the Heap utilization; may be <code>null</code>.
     */
    public Long getMaxHeapInBytes() {
        String value = getProperties().get(MAX_HEAP_IN_BYTES);
        if (value == null) return null;
        return Long.valueOf(value);
    }
}
