/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ReplicationRedoLogOverflowToDiskAlert;
import org.openspaces.admin.alert.alerts.ReplicationRedoLogSizeAlert;
import org.openspaces.admin.alert.config.ReplicationRedoLogSizeAlertConfiguration;
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
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingReplication;

public class ReplicationRedoLogOverflowToDiskAlertBean implements AlertBean, SpaceInstanceRemovedEventListener, SpaceInstanceStatisticsChangedEventListener {

    public static final String beanUID = "3519ba78-08e6de85-87dc-4c10-8d08-ef03fe7b5d76";
    public static final String ALERT_NAME = "Replication Redo log Overflow";
    
    private final ReplicationRedoLogSizeAlertConfiguration config = new ReplicationRedoLogSizeAlertConfiguration();

    private Admin admin;

    public ReplicationRedoLogOverflowToDiskAlertBean() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getSpaces().getSpaceInstanceRemoved().add(this);
        admin.getSpaces().getSpaceInstanceStatisticsChanged().add(this);
        admin.getSpaces().startStatisticsMonitor();
    }

    @Override
    public void destroy() throws Exception {
        admin.getSpaces().getSpaceInstanceRemoved().remove(this);
        admin.getSpaces().getSpaceInstanceStatisticsChanged().remove(this);
        admin.getSpaces().stopStatisticsMonitor();
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
            admin.getAlertManager().triggerAlert( new ReplicationRedoLogOverflowToDiskAlert(alert));
        }
    }
    
    
    @Override
    public void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event) {
        
        final SpaceInstance source = event.getSpaceInstance();
        final ReplicationStatistics replicationStatistics = event.getStatistics().getReplicationStatistics();
        if (replicationStatistics == null) return;
        
        OutgoingReplication outgoingReplication = replicationStatistics.getOutgoingReplication();
        long redoLogSize = outgoingReplication.getRedoLogSize();
        long redoLogSizeInDisk = outgoingReplication.getRedoLogExternalStoragePacketCount();
        long redoLogSizeInMemory = outgoingReplication.getRedoLogMemoryPacketCount();
        
        if (redoLogSizeInDisk > 0) {
            final String groupUid = generateGroupUid(source.getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Replication redo-log has overflown to disk, for " + getSpaceName(source));
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(source.getUid());
            factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(source));
            factory.config(config.getProperties());
            
            factory.putProperty(ReplicationRedoLogSizeAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
            factory.putProperty(ReplicationRedoLogSizeAlert.HOST_NAME, source.getMachine().getHostName());
            factory.putProperty(ReplicationRedoLogSizeAlert.VIRTUAL_MACHINE_UID, source.getVirtualMachine().getUid());
            factory.putProperty(ReplicationRedoLogSizeAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
            factory.putProperty(ReplicationRedoLogSizeAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
            
            factory.putProperty(ReplicationRedoLogSizeAlert.REPLICATION_STATUS, getReplicationStatus(source));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SIZE, String.valueOf(redoLogSize));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_MEMORY_SIZE, String.valueOf(redoLogSizeInMemory));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SWAP_SIZE, String.valueOf(redoLogSizeInDisk));

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new ReplicationRedoLogOverflowToDiskAlert(alert));
            
        } else if (redoLogSizeInDisk == 0 && redoLogSize >= 0){
            final String groupUid = generateGroupUid(source.getUid());
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Replication redo-log no longer uses the disk, for " + getSpaceName(source));
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getSpaceInstance().getUid());
                factory.componentDescription(AlertBeanUtils.getSpaceInstanceDescription(source));
                factory.config(config.getProperties());
                
                factory.putProperty(ReplicationRedoLogSizeAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
                factory.putProperty(ReplicationRedoLogSizeAlert.HOST_NAME, source.getMachine().getHostName());
                factory.putProperty(ReplicationRedoLogSizeAlert.VIRTUAL_MACHINE_UID, source.getVirtualMachine().getUid());
                factory.putProperty(ReplicationRedoLogSizeAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ReplicationRedoLogSizeAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                
                factory.putProperty(ReplicationRedoLogSizeAlert.REPLICATION_STATUS, getReplicationStatus(source));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SIZE, String.valueOf(redoLogSize));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_MEMORY_SIZE, String.valueOf(redoLogSizeInMemory));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SWAP_SIZE, String.valueOf(redoLogSizeInDisk));

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert(new ReplicationRedoLogOverflowToDiskAlert(alert));
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
