package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ReplicationChannelDisconnectedAlert;
import org.openspaces.admin.alert.config.ReplicationChannelDisconnectedAlertConfiguration;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;

public class ReplicationChannelDisconnectedAlertBean implements AlertBean, ReplicationStatusChangedEventListener, SpaceInstanceRemovedEventListener {

    public static final String beanUID = "c54333ba-5d8ed065-4ac9-4f1e-a90e-236e36a1bc71";
    public static final String ALERT_NAME = "Replication Channel Disconnected";
    
    private final ReplicationChannelDisconnectedAlertConfiguration config = new ReplicationChannelDisconnectedAlertConfiguration();

    private Admin admin;

    public ReplicationChannelDisconnectedAlertBean() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getSpaces().getSpaceInstanceRemoved().add(this);
        admin.getSpaces().getReplicationStatusChanged().add(this);
    }

    @Override
    public void destroy() throws Exception {
        admin.getSpaces().getSpaceInstanceRemoved().remove(this);
        admin.getSpaces().getReplicationStatusChanged().remove(this);
    }

    @Override
    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }

    private void validateProperties() {
    }
    
    @Override
    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        for (ReplicationTarget replicationTarget : spaceInstance.getReplicationTargets()) {
            final String groupUid = generateGroupUid(spaceInstance.getUid()+"-"+replicationTarget.getMemberName());
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Replication channel status is unavailable; " + spaceInstance + " has been removed.");
                factory.severity(AlertSeverity.SEVERE);
                factory.status(AlertStatus.NA);
                factory.componentUid(spaceInstance.getUid());
                factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(spaceInstance));
                factory.config(config.getProperties());
                factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_IS_MIRROR, Boolean.toString(replicationTarget.isMirror()));
                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ReplicationChannelDisconnectedAlert(alert));
            }
        }
    }
    
    
    @Override
    public void replicationStatusChanged(ReplicationStatusChangedEvent event) {

        final ReplicationStatus replicationStatus = event.getNewStatus();
        final SpaceInstance source = event.getSpaceInstance();
        final SpaceInstance target = event.getReplicationTarget().getSpaceInstance();
        
        switch (replicationStatus) {
            case DISCONNECTED: {
                final String groupUid = generateGroupUid(source.getUid()+"-"+event.getReplicationTarget().getMemberName());
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("A replication channel has been lost between " + getReplicationPath(event));
                factory.severity(AlertSeverity.SEVERE);
                factory.status(AlertStatus.RAISED);
                factory.componentUid(source.getUid());
                factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(source));
                factory.config(config.getProperties());
                
                factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_HOST_ADDRESS, source.getMachine().getHostAddress());
                factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_HOST_NAME, source.getMachine().getHostName());
                factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                
                factory.putProperty(ReplicationChannelDisconnectedAlert.REPLICATION_STATUS, replicationStatus.name());
                factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_UID, source.getUid());
                
                factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_IS_MIRROR, Boolean.toString(event.getReplicationTarget().isMirror()));
                if (target != null) {
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_UID, target.getUid());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_HOST_ADDRESS, target.getMachine().getHostAddress());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_HOST_NAME, target.getMachine().getHostName());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_CPU_UTILIZATION, String.valueOf(target.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_HEAP_UTILIZATION, String.valueOf(target.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                }

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new ReplicationChannelDisconnectedAlert(alert));
                break;
            }
            case ACTIVE: {
                final String groupUid = generateGroupUid(source.getUid()+"-"+event.getReplicationTarget().getMemberName());
                Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
                if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                    AlertFactory factory = new AlertFactory();
                    factory.name(ALERT_NAME);
                    factory.groupUid(groupUid);
                    factory.description("A replication channel has been restored between " + getReplicationPath(event));
                    factory.severity(AlertSeverity.SEVERE);
                    factory.status(AlertStatus.RESOLVED);
                    factory.componentUid(source.getUid());
                    factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(source));
                    factory.config(config.getProperties());
                    
                    factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_HOST_ADDRESS, source.getMachine().getHostAddress());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_HOST_NAME, source.getMachine().getHostName());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                    factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                    
                    factory.putProperty(ReplicationChannelDisconnectedAlert.REPLICATION_STATUS, replicationStatus.name());
                    factory.putProperty(ReplicationChannelDisconnectedAlert.SOURCE_UID, source.getUid());
                    
                    factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_IS_MIRROR, Boolean.toString(event.getReplicationTarget().isMirror()));
                    if (target != null) {
                        factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_UID, target.getUid());
                        factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_HOST_ADDRESS, target.getMachine().getHostAddress());
                        factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_HOST_NAME, target.getMachine().getHostName());
                        factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_CPU_UTILIZATION, String.valueOf(target.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                        factory.putProperty(ReplicationChannelDisconnectedAlert.TARGET_HEAP_UTILIZATION, String.valueOf(target.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                    }
                    
                    Alert alert = factory.toAlert();
                    admin.getAlertManager().triggerAlert( new ReplicationChannelDisconnectedAlert(alert));
                }
                break;
            }
        }
    }

    /**
     * Try and extract the replication path
     * Primary -> Backup
     * Primary -> Mirror
     * Backup -- soon to be elected as Primary -> new Backup
     * 
     * In each case, where an instance has failed, the SpaceInstance may be null.
     * Thus if a backup failed, or a mirror failed or a primary has failed, we need to
     * sometimes guess what the target was. In case of a mirror, we assume that the mirror
     * may be called by any name (not just mirror-service; since 8.0), so check against the
     * cluster name (e.g. ending with ":foo").
     */
    private String getReplicationPath(ReplicationStatusChangedEvent event) {

        final SpaceInstance source = event.getSpaceInstance();
        final SpaceInstance target = event.getReplicationTarget().getSpaceInstance();
        StringBuilder sb = new StringBuilder();
        SpaceMode sourceSpaceMode = source.getMode();
        sb.append(sourceSpaceMode.toString().toLowerCase()).append(" Space ");
        sb.append(source.getSpace().getName() + "." + source.getInstanceId() + " ["+(source.getBackupId()+1)+"]");
        sb.append(" and ");
        if (target != null) {
            String memberName = event.getReplicationTarget().getMemberName();
            if (memberName.endsWith(":"+source.getSpace().getName())) {
                if (!target.getMode().equals(SpaceMode.NONE)) {
                    sb.append(target.getMode().toString().toLowerCase());
                }
            }
            sb.append("Space ").append(target.getSpace().getName() + "." + target.getInstanceId() + " ["+(target.getBackupId()+1)+"]");
        } else {
            String memberName = event.getReplicationTarget().getMemberName();
            if (memberName.endsWith(":"+source.getSpace().getName())) {
                sb.append("Space ").append(source.getSpace().getName() + "." + source.getInstanceId() + " ["+((source.getBackupId()!=0)?"1":"2")+"]");
            } else {
                sb.append("Space ").append(memberName);
            }
        }
        return sb.toString();
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
