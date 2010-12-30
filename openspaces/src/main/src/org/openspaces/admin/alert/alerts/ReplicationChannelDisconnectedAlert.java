package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.ReplicationChannelDisconnectedAlertBeanConfigurer;
import org.openspaces.admin.alert.events.AlertEventListener;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.SpaceInstance;

/**
 * A replication channel disconnection alert, fired upon a disconnected channel between a primary
 * Space and it's backup Space. The alert is raised when the channel has disconnected. The alert is
 * resolved when the channel is reconnected.
 * <p>
 * These thresholds can be configured by using the
 * {@link ReplicationChannelDisconnectedAlertBeanConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertEventListener#onAlert(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationChannelDisconnectedAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    
    public static final String REPLICATION_STATUS = "replication-status";
    public static final String SOURCE_UID = "source-uid";
    public static final String TARGET_UID = "target-uid";
    
    public ReplicationChannelDisconnectedAlert(Alert alert) {
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
     * The heap memory utilization reading when this alert was fired.
     * @return the heap utilization; may be <code>null</code>.
     */
    public Double getHeapUtilization() {
        String value = getProperties().get(HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The {@link ReplicationStatus replication status} from source to target Space.
     * @return the replication status; may be <code>null</code>.
     */
    public String getReplicationStatus() {
        return getProperties().get(REPLICATION_STATUS);
    }
    
    /**
     * The {@link SpaceInstance#getUid() source Space unique id}.
     * @return the source uid; may be <code>null</code>.
     */
    public String getSourceUid() {
        return getProperties().get(SOURCE_UID);
    }
    
    /**
     * The {@link SpaceInstance#getUid() target Space unique id}.
     * @return the target uid; may be <code>null</code>.
     */
    public String getTargetUid() {
        return getProperties().get(TARGET_UID);
    }
}
