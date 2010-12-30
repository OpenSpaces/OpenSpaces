package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ReplicationChannelDisconnectedAlert;
import org.openspaces.admin.alert.config.ReplicationChannelDisconnectedAlertBeanConfig;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;

public class ReplicationChannelDisconnectedAlertBean implements AlertBean, ReplicationStatusChangedEventListener, SpaceInstanceRemovedEventListener {

    public static final String beanUID = "c54333ba-5d8ed065-4ac9-4f1e-a90e-236e36a1bc71";
    public static final String ALERT_NAME = "Replication Channel Disconnected";
    
    private final ReplicationChannelDisconnectedAlertBeanConfig config = new ReplicationChannelDisconnectedAlertBeanConfig();

    private Admin admin;

    public ReplicationChannelDisconnectedAlertBean() {
    }

    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getSpaces().getSpaceInstanceRemoved().add(this);
        admin.getSpaces().getReplicationStatusChanged().add(this);
    }

    public void destroy() throws Exception {
        admin.getSpaces().getSpaceInstanceRemoved().remove(this);
        admin.getSpaces().getReplicationStatusChanged().remove(this);
    }

    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }

    private void validateProperties() {
    }
    
    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        final String groupUid = generateGroupUid(spaceInstance.getUid());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.description("Replication channel status is unavailable; " + spaceInstance + " has been removed.");
        factory.severity(AlertSeverity.SEVERE);
        factory.status(AlertStatus.NA);
        factory.componentUid(spaceInstance.getUid());
        factory.config(config.getProperties());

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert( new ReplicationChannelDisconnectedAlert(alert));
    }
    
    
    public void replicationStatusChanged(ReplicationStatusChangedEvent event) {
        
        if (event.getPreviousStatus() == null) {
            return; //wait for full statistics. Avoid alert upon first deployment.
        }
        
        final ReplicationStatus replicationStatus = event.getNewStatus();
        final SpaceInstance source = event.getSpaceInstance();
        final SpaceInstance target = event.getReplicationTarget().getSpaceInstance();
        
        switch (replicationStatus) {
            case DISCONNECTED: {
                final String groupUid = generateGroupUid(source.getUid());
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("A replication channel has been lost between " + getReplicationPath(source, target));
                factory.severity(AlertSeverity.SEVERE);
                factory.status(AlertStatus.RAISED);
                factory.componentUid(source.getUid());
                factory.config(config.getProperties());
                
                factory.putProperty(ReplicationChannelDisconnectedAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
                factory.putProperty(ReplicationChannelDisconnectedAlert.HOST_NAME, source.getMachine().getHostName());
                factory.putProperty(ReplicationChannelDisconnectedAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ReplicationChannelDisconnectedAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                
                factory.putProperty(ReplicationChannelDisconnectedAlert.REPLICATION_STATUS, replicationStatus.name());
                factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_UID, source.getUid());
                factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_UID, (target == null ? null : target.getUid()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert( new ReplicationChannelDisconnectedAlert(alert));
                break;
            }
            case ACTIVE: {
                final String groupUid = generateGroupUid(source.getUid());
                AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
                AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
                if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                    AlertFactory factory = new AlertFactory();
                    factory.name(ALERT_NAME);
                    factory.groupUid(groupUid);
                    factory.description("A replication channel has been restored between " + getReplicationPath(source, target));
                    factory.severity(AlertSeverity.SEVERE);
                    factory.status(AlertStatus.RESOLVED);
                    factory.componentUid(source.getUid());
                    factory.config(config.getProperties());
                    
                    factory.putProperty(ReplicationChannelDisconnectedAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.HOST_NAME, source.getMachine().getHostName());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                    factory.putProperty(ReplicationChannelDisconnectedAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                    
                    factory.putProperty(ReplicationChannelDisconnectedAlert.REPLICATION_STATUS, replicationStatus.name());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_UID, source.getUid());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_UID, (target == null ? null : target.getUid()));
                    
                    Alert alert = factory.toAlert();
                    admin.getAlertManager().fireAlert( new ReplicationChannelDisconnectedAlert(alert));
                }
                break;
            }
        }
    }
    
    private String getReplicationPath(SpaceInstance source, SpaceInstance target) {
        StringBuilder sb = new StringBuilder();
        SpaceMode sourceSpaceMode = source.getMode();
        sb.append(sourceSpaceMode.toString().toLowerCase()).append(" Space ");
        sb.append(source.getSpace().getName() + "." + source.getInstanceId() + " ["+(source.getBackupId()+1)+"]");
        if (sourceSpaceMode.equals(SpaceMode.PRIMARY)) {
            sb.append(" to backup Space ");
        } else { 
            sb.append(" to unavailable primary Space ");
        }
        if (target != null) {
            sb.append(target.getSpace().getName() + "." + target.getInstanceId() + " ["+(target.getBackupId()+1)+"]");
        } else {
            sb.append(source.getSpace().getName() + "." + source.getInstanceId() + " ["+((source.getBackupId()!=0)?"1":"2")+"]");
        }
        return sb.toString();
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
