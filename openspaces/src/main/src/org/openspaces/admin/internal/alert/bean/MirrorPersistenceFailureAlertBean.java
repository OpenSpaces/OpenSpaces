package org.openspaces.admin.internal.alert.bean;

import java.util.List;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.MirrorPersistenceFailureAlert;
import org.openspaces.admin.alert.config.MirrorPersistenceFailureAlertConfiguration;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.cluster.replication.async.mirror.MirrorStatistics;
import com.j_spaces.core.filters.ReplicationStatistics;
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingChannel;
import com.j_spaces.core.filters.ReplicationStatistics.ReplicationMode;

public class MirrorPersistenceFailureAlertBean implements AlertBean, SpaceInstanceRemovedEventListener, SpaceInstanceStatisticsChangedEventListener {

    public static final String beanUID = "aafb1222-f271090d-157b-4aa7-bc99-f5baec11296a";
    public static final String ALERT_NAME = "Mirror Persistence Failure";
    
    private final MirrorPersistenceFailureAlertConfiguration config = new MirrorPersistenceFailureAlertConfiguration();

    private Admin admin;

    public MirrorPersistenceFailureAlertBean() {
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
    }
    
    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        if (!spaceInstance.getSpaceUrl().getSchema().equals("mirror")) {
            return; //not a mirror
        }
        final String groupUid = generateGroupUid(spaceInstance.getUid());
        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Mirror space " + getSpaceName(spaceInstance) + " is unavailable.");
            factory.severity(AlertSeverity.SEVERE);
            factory.status(AlertStatus.NA);
            factory.componentUid(spaceInstance.getUid());
            factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(spaceInstance));
            factory.config(config.getProperties());
            factory.putProperty(MirrorPersistenceFailureAlert.HOST_NAME, spaceInstance.getMachine().getHostName());
            factory.putProperty(MirrorPersistenceFailureAlert.HOST_ADDRESS, spaceInstance.getMachine().getHostAddress());

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new MirrorPersistenceFailureAlert(alert));
        }
    }
    
    
    public void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event) {
        
        final SpaceInstance source = event.getSpaceInstance();
        final ReplicationStatistics replicationStatistics = event.getStatistics().getReplicationStatistics();
        if (replicationStatistics == null) return;
        
        Boolean mirrorChannelIsInconsistent = null;
        String reason = null;
        long redoLogRetainedSize = -1;
        SpaceInstance mirrorInstance = null;
        List<OutgoingChannel> channels = replicationStatistics.getOutgoingReplication().getChannels();
        for (OutgoingChannel channel : channels) {
            if (ReplicationMode.MIRROR.equals(channel.getReplicationMode())) {
                //find matching mirror instance
                for (ReplicationTarget replicationTarget : source.getReplicationTargets()) {
                    if (replicationTarget.getMemberName().equals(channel.getTargetMemberName())) {
                        mirrorInstance = replicationTarget.getSpaceInstance();
                    }
                }
                //extract info from channel
                mirrorChannelIsInconsistent = Boolean.valueOf(channel.isInconsistent());
                if (mirrorChannelIsInconsistent) {
                    reason = channel.getInconsistencyReason();
                    redoLogRetainedSize = channel.getRedologRetainedSize();
                    break;
                }
            }
        }
        
        //no mirror channel found
        if (mirrorChannelIsInconsistent == null || mirrorInstance == null) 
            return;
        
        if (mirrorChannelIsInconsistent && reason != null) {
            final String groupUid = generateGroupUid(mirrorInstance.getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Mirror failed to persist data sent from " + getSpaceName(source) + " - " + getRootCauseMessage(reason));
            factory.severity(AlertSeverity.SEVERE);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(mirrorInstance.getUid());
            factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(mirrorInstance));
            factory.config(config.getProperties());
            
            factory.putProperty(MirrorPersistenceFailureAlert.HOST_NAME, mirrorInstance.getMachine().getHostName());
            factory.putProperty(MirrorPersistenceFailureAlert.HOST_ADDRESS, mirrorInstance.getMachine().getHostAddress());
            
            factory.putProperty(MirrorPersistenceFailureAlert.INCONSISTENCY_REASON, reason);
            factory.putProperty(MirrorPersistenceFailureAlert.ROOT_CAUSE_MESSAGE, getRootCauseMessage(reason));
            factory.putProperty(MirrorPersistenceFailureAlert.ROOT_CAUSE_TRACE, getRootCauseTrace(reason));
            factory.putProperty(MirrorPersistenceFailureAlert.REPLICATION_STATUS, getReplicationStatus(source));
            factory.putProperty(MirrorPersistenceFailureAlert.REDO_LOG_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogSize()));
            factory.putProperty(MirrorPersistenceFailureAlert.REDO_LOG_RETAINED_SIZE, String.valueOf(redoLogRetainedSize));
            
            MirrorStatistics mirrorStatistics = source.getStatistics().getMirrorStatistics();
            if (mirrorStatistics != null) {
                factory.putProperty(MirrorPersistenceFailureAlert.FAILED_OPERATION_COUNT, String.valueOf(mirrorStatistics.getFailedOperationCount()));
                factory.putProperty(MirrorPersistenceFailureAlert.IN_PROGRESS_OPERATION_COUNT, String.valueOf(mirrorStatistics.getInProgressOperationCount()));
                factory.putProperty(MirrorPersistenceFailureAlert.DISCARDED_OPERATION_COUNT, String.valueOf(mirrorStatistics.getDiscardedOperationCount()));
            }

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new MirrorPersistenceFailureAlert(alert));
            
        } else if (!mirrorChannelIsInconsistent) {
            final String groupUid = generateGroupUid(mirrorInstance.getUid());
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Mirror managed to persist data sent from  " + getSpaceName(source));
                factory.severity(AlertSeverity.SEVERE);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(mirrorInstance.getUid());
                factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(mirrorInstance));
                factory.config(config.getProperties());
                
                factory.putProperty(MirrorPersistenceFailureAlert.HOST_NAME, mirrorInstance.getMachine().getHostName());
                factory.putProperty(MirrorPersistenceFailureAlert.HOST_ADDRESS, mirrorInstance.getMachine().getHostAddress());
                
                factory.putProperty(MirrorPersistenceFailureAlert.REPLICATION_STATUS, getReplicationStatus(source));
                factory.putProperty(MirrorPersistenceFailureAlert.REDO_LOG_SIZE, String.valueOf(replicationStatistics.getOutgoingReplication().getRedoLogSize()));
                factory.putProperty(MirrorPersistenceFailureAlert.REDO_LOG_RETAINED_SIZE, String.valueOf(redoLogRetainedSize));
                
                MirrorStatistics mirrorStatistics = source.getStatistics().getMirrorStatistics();
                if (mirrorStatistics != null) {
                    factory.putProperty(MirrorPersistenceFailureAlert.FAILED_OPERATION_COUNT, String.valueOf(mirrorStatistics.getFailedOperationCount()));
                    factory.putProperty(MirrorPersistenceFailureAlert.IN_PROGRESS_OPERATION_COUNT, String.valueOf(mirrorStatistics.getInProgressOperationCount()));
                    factory.putProperty(MirrorPersistenceFailureAlert.DISCARDED_OPERATION_COUNT, String.valueOf(mirrorStatistics.getDiscardedOperationCount()));
                }
                
                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert(new MirrorPersistenceFailureAlert(alert));
            }
        }
    }
    
    private String getRootCauseMessage(String reason) {
        int lastIndexOfCausedBy = reason.lastIndexOf("Caused by: ");
        if (lastIndexOfCausedBy == -1) {
            lastIndexOfCausedBy = 0;
        }
        int endOfLineIndex = reason.indexOf("\n", lastIndexOfCausedBy);
        if (endOfLineIndex == -1) {
            return reason.substring(0, Math.min(20, reason.length()))+"...";
        }
        String cause = reason.substring(lastIndexOfCausedBy, endOfLineIndex);
        return cause;
        
    }
    
    private String getRootCauseTrace(String reason) {
        int lastIndexOfCausedBy = reason.lastIndexOf("Caused by: ");
        if (lastIndexOfCausedBy == -1) {
            lastIndexOfCausedBy = 0;
        }
        int lastIndexOfAt = reason.lastIndexOf("\tat", reason.length());
        if (lastIndexOfAt == -1) {
            return reason.substring(0, Math.min(20, reason.length()))+"...";
        }
        String cause = reason.substring(lastIndexOfCausedBy, lastIndexOfAt);
        return cause;
        
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
