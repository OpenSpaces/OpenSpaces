package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ReplicationRedoLogSizeAlert;
import org.openspaces.admin.alert.config.ReplicationRedoLogSizeAlertConfiguration;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.filters.ReplicationStatistics;

public class ReplicationRedoLogSizeAlertBean implements AlertBean, SpaceInstanceRemovedEventListener, SpaceInstanceStatisticsChangedEventListener {

    public static final String beanUID = "3f4bff98-52de6d72-b2b8-434b-aa18-d57c7554262a";
    public static final String ALERT_NAME = "Replication Redo log";
    
    private final ReplicationRedoLogSizeAlertConfiguration config = new ReplicationRedoLogSizeAlertConfiguration();

    private Admin admin;

    public ReplicationRedoLogSizeAlertBean() {
    }

    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getSpaces().getSpaceInstanceRemoved().add(this);
        admin.getSpaces().getSpaceInstanceStatisticsChanged().add(this);
        admin.getSpaces().startStatisticsMonitor();
    }

    public void destroy() throws Exception {
        admin.getSpaces().getSpaceInstanceRemoved().remove(this);
        admin.getSpaces().getSpaceInstanceStatisticsChanged().remove(this);
        admin.getSpaces().stopStatisticsMonitor();
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
        
        if (config.getHighThresholdRedoLogSize() == null) {
            throw new BeanConfigurationException("High threshold property is null");
        }
        
        if (config.getLowThresholdRedoLogSize() == null) {
            throw new BeanConfigurationException("Low threshold property is null");
        }

        if (config.getHighThresholdRedoLogSize() < config.getLowThresholdRedoLogSize()) {
            throw new BeanConfigurationException("Low threshold [" + config.getLowThresholdRedoLogSize()
                    + "] must be less than high threshold value [" + config.getHighThresholdRedoLogSize() + "]");
        }

        if (config.getHighThresholdRedoLogSize() < 0) {
            throw new BeanConfigurationException("High threshold [" + config.getHighThresholdRedoLogSize()
                    + "] must greater than zero");
        }

        if (config.getLowThresholdRedoLogSize() < 0) {
            throw new BeanConfigurationException("Low threshold [" + config.getLowThresholdRedoLogSize()
                    + "] must greater or equal to zero");
        }
    }
    
    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        final String groupUid = generateGroupUid(spaceInstance.getUid());
        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Replication redo log is unvailable; " + getSpaceName(spaceInstance) + " has been removed.");
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.NA);
            factory.componentUid(spaceInstance.getUid());
            factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(spaceInstance));
            factory.config(config.getProperties());

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ReplicationRedoLogSizeAlert(alert));
        }
    }
    
    
    public void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event) {
        
        int highThreshold = config.getHighThresholdRedoLogSize();
        int lowThreshold = config.getLowThresholdRedoLogSize();
        
        final SpaceInstance source = event.getSpaceInstance();
        final ReplicationStatistics replicationStatistics = event.getStatistics().getReplicationStatistics();
        if (replicationStatistics == null) return;
        
        long redoLogSize = replicationStatistics.getOutgoingReplication().getRedoLogSize();
        
        if (redoLogSize > highThreshold) {
            final String groupUid = generateGroupUid(source.getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Replication redo-log size crossed above a " + highThreshold + " threshold, with a size of " + redoLogSize + " for " + getSpaceName(source));
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(source.getUid());
            factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(source));
            factory.config(config.getProperties());
            
            factory.putProperty(ReplicationRedoLogSizeAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
            factory.putProperty(ReplicationRedoLogSizeAlert.HOST_NAME, source.getMachine().getHostName());
            factory.putProperty(ReplicationRedoLogSizeAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
            factory.putProperty(ReplicationRedoLogSizeAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
            
            factory.putProperty(ReplicationRedoLogSizeAlert.REPLICATION_STATUS, getReplicationStatus(source));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SIZE, String.valueOf(redoLogSize));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_MEMORY_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogMemoryPacketCount()));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SWAP_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogExternalStoragePacketCount()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ReplicationRedoLogSizeAlert(alert));
            
        } else if (redoLogSize <= lowThreshold) {
            final String groupUid = generateGroupUid(source.getUid());
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Replication redo-log size crossed below a " + lowThreshold + " threshold, with a size of " + redoLogSize + " for " + getSpaceName(source));
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getSpaceInstance().getUid());
                factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(source));
                factory.config(config.getProperties());
                
                factory.putProperty(ReplicationRedoLogSizeAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
                factory.putProperty(ReplicationRedoLogSizeAlert.HOST_NAME, source.getMachine().getHostName());
                factory.putProperty(ReplicationRedoLogSizeAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ReplicationRedoLogSizeAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                
                factory.putProperty(ReplicationRedoLogSizeAlert.REPLICATION_STATUS, getReplicationStatus(source));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SIZE, String.valueOf(redoLogSize));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_MEMORY_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogMemoryPacketCount()));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SWAP_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogExternalStoragePacketCount()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert(new ReplicationRedoLogSizeAlert(alert));
            }
        }
    }
    
    private String getReplicationStatus(SpaceInstance source) {
        for (ReplicationTarget target : source.getReplicationTargets()) {
            if (!ReplicationStatus.ACTIVE.equals(target.getReplicationStatus())) {
                return target.getReplicationStatus().name();
            }
        }
        return ReplicationStatus.ACTIVE.name();
    }

    private String getSpaceName(SpaceInstance source) {
        StringBuilder sb = new StringBuilder();
        SpaceMode sourceSpaceMode = source.getMode();
        sb.append(sourceSpaceMode.toString().toLowerCase()).append(" Space ");
        sb.append(source.getSpace().getName() + "." + source.getInstanceId() + " ["+(source.getBackupId()+1)+"]");
        return sb.toString();
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
