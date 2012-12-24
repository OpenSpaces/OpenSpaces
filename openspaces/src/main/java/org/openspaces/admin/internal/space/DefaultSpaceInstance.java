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
package org.openspaces.admin.internal.space;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.DefaultReplicationStatusChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceModeChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalReplicationStatusChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceModeChangedEventManager;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.internal.utils.NameUtils;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;
import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.client.spaceproxy.SpaceProxyImpl;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.lookup.SpaceUrlUtils;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.internal.version.PlatformLogicalVersion;
import com.gigaspaces.lrmi.LRMIMonitoringDetails;
import com.gigaspaces.lrmi.LRMIUtilities;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.admin.StatisticsAdmin;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;
import com.j_spaces.core.filters.StatisticsHolder;
import com.j_spaces.kernel.time.SystemTime;

/**
 * @author kimchy
 */
public class DefaultSpaceInstance extends AbstractGridComponent implements InternalSpaceInstance, StatisticsMonitor {

    private final String uid;

    private final ServiceID serviceID;

    private final PUServiceBean puService;

    // direct space proxy
    private volatile ISpaceProxy ijspace;

    // direct giga space
    private volatile GigaSpace gigaSpace;

    private volatile IInternalRemoteJSpaceAdmin spaceAdmin;

    private final SpaceURL spaceURL;

    private final String clusterSchema;

    private final int numberOfInstances;

    private final int numberOfBackups;

    private final int instanceId;

    private final int backupId;

    private volatile Space space;

    private volatile SpacePartition spacePartition;

    private volatile SpaceMode spaceMode = SpaceMode.NONE;

    private static final ReplicationTarget[] NO_REPLICATION_STATUS = new ReplicationTarget[0];

    private volatile ReplicationTarget[] replicationTargets = NO_REPLICATION_STATUS;

    private final InternalSpaceModeChangedEventManager spaceModeChangedEventManager;

    private final InternalReplicationStatusChangedEventManager replicationStatusChangedEventManager;

    private final InternalSpaceInstanceStatisticsChangedEventManager statisticsChangedEventManager;

    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private SpaceInstanceStatistics lastStatistics;
    
    private final SpaceInstanceRuntimeDetails spaceInstanceRuntimeDetails;

    private Future scheduledStatisticsMonitor;

    private int scheduledStatisticsRefCount = 0;

    public DefaultSpaceInstance(ServiceID serviceID, IJSpace directSpace, IInternalRemoteJSpaceAdmin spaceAdmin, InternalAdmin admin, JVMDetails jvmDetails) {
        super(admin, jvmDetails);
        this.puService = null;
        this.uid = serviceID.toString();
        this.serviceID = serviceID;
        setIJSpace((ISpaceProxy) directSpace);
        this.gigaSpace = new GigaSpaceConfigurer(directSpace).gigaSpace();
        this.spaceAdmin = spaceAdmin;
        this.spaceURL = ijspace.getURL();

        this.spaceModeChangedEventManager = new DefaultSpaceModeChangedEventManager(null, admin);
        this.replicationStatusChangedEventManager = new DefaultReplicationStatusChangedEventManager(admin);
        this.statisticsChangedEventManager = new DefaultSpaceInstanceStatisticsChangedEventManager(admin);
        this.spaceInstanceRuntimeDetails = new DefaultSpaceInstanceRuntimeDetails(this);

        this.clusterSchema = spaceURL.getProperty(SpaceURL.CLUSTER_SCHEMA);

        String sInstanceId = spaceURL.getProperty(SpaceURL.CLUSTER_MEMBER_ID);
        if (sInstanceId == null || sInstanceId.length() == 0) {
            instanceId = 1;
        } else {
            instanceId = Integer.parseInt(sInstanceId);
        }
        String sBackupId = spaceURL.getProperty(SpaceURL.CLUSTER_BACKUP_ID);
        if (sBackupId == null || sBackupId.length() == 0) {
            backupId = 0;
        } else {
            backupId = Integer.parseInt(sBackupId);
        }
        String totalMembers = spaceURL.getProperty(SpaceURL.CLUSTER_TOTAL_MEMBERS);
        if (totalMembers == null || totalMembers.length() == 0) {
            numberOfInstances = 1;
            numberOfBackups = 0;
        } else {
            int index = totalMembers.indexOf(',');
            if (index > 0) {
                numberOfInstances = Integer.parseInt(totalMembers.substring(0, index));
                numberOfBackups = Integer.parseInt(totalMembers.substring(index + 1));
            } else {
                numberOfInstances = Integer.parseInt(totalMembers);
                numberOfBackups = 0;
            }
        }
    }

    public DefaultSpaceInstance(PUServiceBean puService, SpaceServiceDetails spaceServiceDetails, InternalAdmin admin, JVMDetails jvmDetails ) {
        super(admin, jvmDetails);
        this.uid = spaceServiceDetails.getServiceID().toString();
        this.serviceID = spaceServiceDetails.getServiceID();
        this.puService = puService;
        this.spaceURL = spaceServiceDetails.getSpaceUrl();

        this.spaceModeChangedEventManager = new DefaultSpaceModeChangedEventManager(null, admin);
        this.replicationStatusChangedEventManager = new DefaultReplicationStatusChangedEventManager(admin);
        this.statisticsChangedEventManager = new DefaultSpaceInstanceStatisticsChangedEventManager(admin);
        this.spaceInstanceRuntimeDetails = new DefaultSpaceInstanceRuntimeDetails(this);

        this.clusterSchema = spaceURL.getProperty(SpaceURL.CLUSTER_SCHEMA);

        String sInstanceId = spaceURL.getProperty(SpaceURL.CLUSTER_MEMBER_ID);
        if (sInstanceId == null || sInstanceId.length() == 0) {
            instanceId = 1;
        } else {
            instanceId = Integer.parseInt(sInstanceId);
        }
        String sBackupId = spaceURL.getProperty(SpaceURL.CLUSTER_BACKUP_ID);
        if (sBackupId == null || sBackupId.length() == 0) {
            backupId = 0;
        } else {
            backupId = Integer.parseInt(sBackupId);
        }
        String totalMembers = spaceURL.getProperty(SpaceURL.CLUSTER_TOTAL_MEMBERS);
        if (totalMembers == null || totalMembers.length() == 0) {
            numberOfInstances = 1;
            numberOfBackups = 0;
        } else {
            int index = totalMembers.indexOf(',');
            if (index > 0) {
                numberOfInstances = Integer.parseInt(totalMembers.substring(0, index));
                numberOfBackups = Integer.parseInt(totalMembers.substring(index + 1));
            } else {
                numberOfInstances = Integer.parseInt(totalMembers);
                numberOfBackups = 0;
            }
        }
    }

    public String getUid() {
        return uid;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    public GigaSpace getGigaSpace() {
        if (gigaSpace == null) {
            gigaSpace = new GigaSpaceConfigurer(getIJSpace()).gigaSpace();
        }
        return this.gigaSpace;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.statisticsInterval = timeUnit.toMillis(interval);
        if (scheduledStatisticsMonitor != null) {
            stopStatisticsMonitor();
            startStatisticsMonitor();
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
    }

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsRefCount++ > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final SpaceInstance spaceInstance = this;
        scheduledStatisticsMonitor = ((InternalAdmin) getAdmin()).scheduleWithFixedDelay(new Runnable() {
            public void run() {
                SpaceInstanceStatistics stats = spaceInstance.getStatistics();
                SpaceInstanceStatisticsChangedEvent event = new SpaceInstanceStatisticsChangedEvent(spaceInstance, stats);
                statisticsChangedEventManager.spaceInstanceStatisticsChanged(event);
                ((InternalSpaceInstanceStatisticsChangedEventManager) space.getInstanceStatisticsChanged()).spaceInstanceStatisticsChanged(event);
                ((InternalSpaceInstanceStatisticsChangedEventManager) space.getSpaces().getSpaceInstanceStatisticsChanged()).spaceInstanceStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopStatisticsMonitor() {
        if (scheduledStatisticsRefCount!=0 && --scheduledStatisticsRefCount > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    public String getClusterSchema() {
        return clusterSchema;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public int getNumberOfBackups() {
        return numberOfBackups;
    }

    public SpaceURL getSpaceUrl() {
        return this.spaceURL;
    }

    public String getSpaceName() {
        return spaceURL.getSpaceName();
    }

    public SpaceModeChangedEventManager getSpaceModeChanged() {
        return this.spaceModeChangedEventManager;
    }

    public ReplicationStatusChangedEventManager getReplicationStatusChanged() {
        return this.replicationStatusChangedEventManager;
    }

    public SpaceInstanceStatisticsChangedEventManager getStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }
    
    public String getSpaceInstanceName() {

        return NameUtils.getSpaceInstanceName( getSpace().getName(), getInstanceId(),
                                        getBackupId(), getSpace().getNumberOfBackups() );
    }

    public int getInstanceId() {
        return instanceId;
    }

    public int getBackupId() {
        return backupId;
    }

    public IJSpace getIJSpace() {
        if (this.ijspace == null) {
            try {
                setIJSpace((ISpaceProxy) puService.getSpaceDirect(serviceID));
                if (this.ijspace.isSecured()) {
                    this.ijspace.login(admin.getUserDetails());
                }
            } catch (RemoteException e) {
                throw new AdminException("Failed to fetch space", e);
            }
        }
        return this.ijspace;
    }
    
    protected void setIJSpace(ISpaceProxy spaceProxy) {
        this.ijspace = spaceProxy;
        if (spaceProxy instanceof SpaceProxyImpl) {
            String url = SpaceUrlUtils.buildJiniUrl(spaceProxy.getURL().getContainerName(), spaceProxy.getURL().getSpaceName(), admin.getGroups(), admin.getLocators());
            try {
                ((SpaceProxyImpl)spaceProxy).setFinderURL(SpaceURLParser.parseURL(url));
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Failed to build finder url", e);
            }
        }
    }
    
    public IInternalRemoteJSpaceAdmin getSpaceAdmin() {
        if (spaceAdmin == null) {
            try {
                this.spaceAdmin = (IInternalRemoteJSpaceAdmin) getIJSpace().getAdmin();
            } catch (RemoteException e) {
                return null;
            }
        }
        return this.spaceAdmin;
    }

    public RuntimeHolder getRuntimeHolder() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getRuntimeHolder();
        }
        return puService.getSpaceRuntimeHolder(serviceID);
    }

    public void setMode(SpaceMode spaceMode) {
        assertStateChangesPermitted();
        SpaceMode previousSpaceMode = this.spaceMode;
        SpaceMode newSpaceMode = spaceMode;
        if (previousSpaceMode != newSpaceMode) {
            SpaceModeChangedEvent event = new SpaceModeChangedEvent(this, previousSpaceMode, newSpaceMode);
            spaceModeChangedEventManager.spaceModeChanged(event);
            ((InternalSpaceModeChangedEventManager) getSpace().getSpaceModeChanged()).spaceModeChanged(event);
            ((InternalSpaceModeChangedEventManager) getSpace().getSpaces().getSpaceModeChanged()).spaceModeChanged(event);
        }
        this.spaceMode = spaceMode;
    }

    public SpaceMode getMode() {
        return this.spaceMode;
    }

    public boolean waitForMode(final SpaceMode requiredMode, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        SpaceModeChangedEventListener changed = new SpaceModeChangedEventListener() {
            public void spaceModeChanged(SpaceModeChangedEvent event) {
                if (event.getNewMode().equals(requiredMode)) {
                    result.set(true);
                    latch.countDown();
                }
            }
        };
        spaceModeChangedEventManager.add(changed);
        try {
            if (spaceMode.equals(requiredMode)) {
                return true;
            }
            latch.await(timeout, timeUnit);
            return result.get();
        } catch (InterruptedException e) {
            return false;
        } finally {
            spaceModeChangedEventManager.remove(changed);
        }
    }

    private static final SpaceInstanceStatistics NA_STATISTICS = new DefaultSpaceInstanceStatistics(new StatisticsHolder(new long[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}), null, 0, -1);

    public synchronized SpaceInstanceStatistics getStatistics() {
        long currentTime = SystemTime.timeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        try {
            if (getVirtualMachine().getMachine() == null) return NA_STATISTICS; //machine has not yet been set

            StatisticsHolder holder = getStatisticsHolder();
            lastStatistics = new DefaultSpaceInstanceStatistics(holder, lastStatistics, statisticsHistorySize, getVirtualMachine().getMachine().getOperatingSystem().getTimeDelta());
        } catch (RemoteException e) {
            lastStatistics = NA_STATISTICS;
        }
        return lastStatistics;
    }
    
    public SpaceInstanceRuntimeDetails getRuntimeDetails() {
        return spaceInstanceRuntimeDetails;
    }

    public ReplicationTarget[] getReplicationTargets() {
        return replicationTargets;
    }

    public void setReplicationTargets(ReplicationTarget[] replicationTargets) {
        assertStateChangesPermitted();
        ReplicationTarget[] previousReplicationTargets = this.replicationTargets;
        ReplicationTarget[] newReplicationTargets = replicationTargets;
        this.replicationTargets = replicationTargets;

        List<ReplicationStatusChangedEvent> events = new ArrayList<ReplicationStatusChangedEvent>();
        if (previousReplicationTargets == NO_REPLICATION_STATUS) {
            for (ReplicationTarget replicationTarget : newReplicationTargets) {
                events.add(new ReplicationStatusChangedEvent(this, replicationTarget, null, replicationTarget.getReplicationStatus()));
            }
        } else {
            for (int i = 0; i < newReplicationTargets.length; i++) {
                ReplicationTarget newReplicationTarget = newReplicationTargets[i];
                ReplicationTarget previousReplicationTarget = locatePreviousReplicationTarget(newReplicationTarget, previousReplicationTargets);
                if (previousReplicationTarget == null) {
                    for (ReplicationTarget replicationTarget : newReplicationTargets) {
                        events.add(new ReplicationStatusChangedEvent(this, replicationTarget, null, replicationTarget.getReplicationStatus()));
                    }
                } else {
                    if (newReplicationTarget.getReplicationStatus() != previousReplicationTarget.getReplicationStatus()) {
                        events.add(new ReplicationStatusChangedEvent(this, newReplicationTarget, previousReplicationTarget.getReplicationStatus(), newReplicationTarget.getReplicationStatus()));
                    }
                }
            }
        }
        for (ReplicationStatusChangedEvent event : events) {
            replicationStatusChangedEventManager.replicationStatusChanged(event);
            ((InternalReplicationStatusChangedEventManager) getSpace().getReplicationStatusChanged()).replicationStatusChanged(event);
            ((InternalReplicationStatusChangedEventManager) getSpace().getSpaces().getReplicationStatusChanged()).replicationStatusChanged(event);
        }
    }

    private ReplicationTarget locatePreviousReplicationTarget(ReplicationTarget newReplicationTarget, ReplicationTarget[] previousReplicationTargets) {
        for (ReplicationTarget prevReplicationTarget : previousReplicationTargets) {
            String pevReplicationTargetMemberName = prevReplicationTarget.getMemberName();
            String newReplicationTargetMemberName = newReplicationTarget.getMemberName();
            if (newReplicationTargetMemberName.equals(pevReplicationTargetMemberName))
                return prevReplicationTarget;
        }
        return null;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        assertStateChangesPermitted();
        this.space = space;
    }

    public void setPartition(SpacePartition spacePartition) {
        assertStateChangesPermitted();
        this.spacePartition = spacePartition;
    }

    public SpacePartition getPartition() {
        return this.spacePartition;
    }

    public NIODetails getNIODetails() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getNIODetails();
        }
        return puService.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getNIOStatistics();
        }
        return puService.getNIOStatistics();
    }
    
    @Override
    public void enableLRMIMonitoring() throws RemoteException {
        if (spaceAdmin != null) {
            spaceAdmin.enableLRMIMonitoring();
        }
        puService.enableLRMIMonitoring();
    }
    
    @Override
    public void disableLRMIMonitoring() throws RemoteException {
        if (spaceAdmin != null) {
            spaceAdmin.disableLRMIMonitoring();
        }
        puService.disableLRMIMonitoring();
    }
    
    @Override
    public LRMIMonitoringDetails fetchLRMIMonitoringDetails() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.fetchLRMIMonitoringDetails();
        }
        return puService.fetchLRMIMonitoringDetails();
    }

    public long getCurrentTimeInMillis() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getCurrentTimestamp();
        }
        return puService.getCurrentTimestamp();
    }

    public OSDetails getOSDetails() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getOSDetails();
        }
        return puService.getOSDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getOSStatistics();
        }
        return puService.getOSStatistics();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        if (spaceAdmin != null) {
            return spaceAdmin.getJVMStatistics();
        }
        return puService.getJVMStatistics();
    }

    public void runGc() throws RemoteException {
        if (spaceAdmin != null) {
            spaceAdmin.runGc();
        }
        puService.runGc();
    }
    
    @Override
    public String toString() {
        String name = getSpaceInstanceName();
        switch(this.spaceMode) {
        case PRIMARY:
            name += "(P)";
            break;
        case BACKUP:
            name += "(B)";
            break;
        }
        return name;
    }

    @Override
    public StatisticsHolder getStatisticsHolder() throws RemoteException {
        if (spaceAdmin != null) {
            return ((StatisticsAdmin) spaceAdmin).getHolder();
        }
        return puService.getSpaceStatisticsHolder(serviceID);
    }

    @Override
    public PlatformLogicalVersion getPlatformLogicalVersion() {
        return LRMIUtilities.getServicePlatformLogicalVersion(((ISpaceProxy)getIJSpace()).getDirectProxy().getRemoteJSpace());
    }
}
