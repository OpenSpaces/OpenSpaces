package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.GarbageCollectionAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * A Garbage Collection duration alert, fired upon triggered GC thresholds. The alert is raised when
 * the JVM spends more than a specified period of time on Garbage Collection. The alert is resolved
 * when the JVM spends less then the specified time on Garbage Collection.
 * <p>
 * These thresholds can be configured by using the {@link GarbageCollectionAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class GarbageCollectionAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    
    public static final String PROCESS_ID = "process-id";
    public static final String COMPONENT_NAME = "component-name";
    public static final String GC_DURATION_MILLISECONDS = "gc-duration-milliseconds";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    public static final String NON_HEAP_UTILIZATION = "non-heap-utilization";

    /** required by java.io.Externalizable */
    public GarbageCollectionAlert() {
    }
    
    public GarbageCollectionAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link VirtualMachine#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
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
     * The non-Heap utilization reading when this alert was fired.
     * @return the non-Heap utilization; may be <code>null</code>.
     */
    public Double getNonHeapUtilization() {
        String value = getProperties().get(NON_HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The period of time the JVM has spent on GC which triggered the alert.
     * 
     * @return the GC duration in milliseconds.
     */
    public Long getGcDuration() {
        String value = getProperties().get(GC_DURATION_MILLISECONDS);
        if (value == null) return null;
        return Long.valueOf(value);
    }
}
