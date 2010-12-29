package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.config.ReplicationRedoLogSizeAlertBeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.filters.ReplicationStatistics;
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingReplication;

public class ReplicationRedoLogSizeAlertBean implements AlertBean, SpaceInstanceRemovedEventListener, SpaceInstanceStatisticsChangedEventListener {

    public static final String beanUID = "3f4bff98-52de6d72-b2b8-434b-aa18-d57c7554262a";
    public static final String ALERT_NAME = "Replication Redo log";
    public static final String SOURCE_UID = "source-uid";
    public static final String REDO_LOG_SIZE = "redo-log-size";
    public static final String REDO_LOG_MEMORY_SIZE = "redo-log-memory-size";
    public static final String REDO_LOG_SWAP_SIZE = "redo-log-swap-size";
    
    private final ReplicationRedoLogSizeAlertBeanConfig config = new ReplicationRedoLogSizeAlertBeanConfig();

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
        try {
            config.getHighThresholdRedoLogSize();
            config.getLowThresholdRedoLogSize();
        } catch (IllegalArgumentException e) {
            throw new BeanConfigurationException(e.getMessage());
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
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.description("Replication redo log is unvailable; " + getSpaceName(spaceInstance) + " has been removed.");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(spaceInstance.getUid());
        factory.properties(config.getProperties());
        factory.putProperty(SOURCE_UID, spaceInstance.getUid());
        OutgoingReplication outgoingReplication = spaceInstance.getStatistics().getReplicationStatistics().getOutgoingReplication();
        factory.putProperty(REDO_LOG_SIZE, String.valueOf(outgoingReplication.getRedoLogSize()));
        factory.putProperty(REDO_LOG_MEMORY_SIZE, String.valueOf(outgoingReplication.getRedoLogMemoryPacketCount()));
        factory.putProperty(REDO_LOG_SWAP_SIZE, String.valueOf(outgoingReplication.getRedoLogExternalStoragePacketCount()));

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert(alert);
    }
    
    
    public void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event) {
        
        int highThreshold = config.getHighThresholdRedoLogSize();
        int lowThreshold = config.getLowThresholdRedoLogSize();
        
        final SpaceInstance source = event.getSpaceInstance();
        final ReplicationStatistics replicationStatistics = event.getStatistics().getReplicationStatistics();

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
            factory.properties(config.getProperties());
            factory.putProperty(SOURCE_UID, source.getUid());
            factory.putProperty(REDO_LOG_SIZE, String.valueOf(redoLogSize));
            factory.putProperty(REDO_LOG_MEMORY_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogMemoryPacketCount()));
            factory.putProperty(REDO_LOG_SWAP_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogExternalStoragePacketCount()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().fireAlert(alert);
        } else if (redoLogSize <= lowThreshold) {
            final String groupUid = generateGroupUid(source.getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Replication redo-log size crossed below a " + lowThreshold + " threshold, with a size of " + redoLogSize + " for " + getSpaceName(source));
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getSpaceInstance().getUid());
                factory.properties(config.getProperties());
                factory.putProperty(SOURCE_UID, source.getUid());
                factory.putProperty(REDO_LOG_SIZE, String.valueOf(redoLogSize));
                factory.putProperty(REDO_LOG_MEMORY_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogMemoryPacketCount()));
                factory.putProperty(REDO_LOG_SWAP_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogExternalStoragePacketCount()));                
                
                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert(alert);
            }
        }
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
