package org.openspaces.admin.internal.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.admin.StatisticsAdmin;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.filters.StatisticsHolder;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.DefaultReplicationStatusChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceModeChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalReplicationStatusChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceModeChangedEventManager;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultSpaceInstance extends AbstractGridComponent implements InternalSpaceInstance, StatisticsMonitor {

    private final String uid;

    private final ServiceID serviceID;

    // direct space proxy
    private final IJSpace ijspace;

    // direct giga space
    private final GigaSpace gigaSpace;

    private final IInternalRemoteJSpaceAdmin spaceAdmin;

    private final SpaceConfig spaceConfig;

    private final SpaceURL spaceURL;

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

    private Future scheduledStatisticsMonitor;

    public DefaultSpaceInstance(ServiceID serviceID, IJSpace directSpace, IInternalRemoteJSpaceAdmin spaceAdmin,
                                SpaceConfig spaceConfig, InternalAdmin admin) {
        super(admin);
        this.uid = serviceID.toString();
        this.serviceID = serviceID;
        this.ijspace = directSpace;
        this.gigaSpace = new GigaSpaceConfigurer(directSpace).gigaSpace();
        this.spaceAdmin = spaceAdmin;
        this.spaceConfig = spaceConfig;
        this.spaceURL = ijspace.getURL();

        this.spaceModeChangedEventManager = new DefaultSpaceModeChangedEventManager(null, admin);
        this.replicationStatusChangedEventManager = new DefaultReplicationStatusChangedEventManager(admin);
        this.statisticsChangedEventManager = new DefaultSpaceInstanceStatisticsChangedEventManager(admin);

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
        return this.gigaSpace;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.statisticsInterval = timeUnit.toMillis(interval);
        if (scheduledStatisticsMonitor != null) {
            stopStatisticsMontior();
            startStatisticsMonitor();
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
    }

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final SpaceInstance spaceInstance = this;
        scheduledStatisticsMonitor = ((InternalAdmin) getAdmin()).getScheduler().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                SpaceInstanceStatistics stats = spaceInstance.getStatistics();
                SpaceInstanceStatisticsChangedEvent event = new SpaceInstanceStatisticsChangedEvent(spaceInstance, stats);
                statisticsChangedEventManager.spaceInstanceStatisticsChanged(event);
                ((InternalSpaceInstanceStatisticsChangedEventManager) space.getInstanceStatisticsChanged()).spaceInstanceStatisticsChanged(event);
                ((InternalSpaceInstanceStatisticsChangedEventManager) space.getSpaces().getSpaceInstanceStatisticsChanged()).spaceInstanceStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopStatisticsMontior() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public int getNumberOfBackups() {
        return numberOfBackups;
    }

    public String getSpaceName() {
        return spaceConfig.getSpaceName();
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

    public int getInstanceId() {
        return instanceId;
    }

    public int getBackupId() {
        return backupId;
    }

    public SpaceConfig getSpaceConfig() {
        return this.spaceConfig;
    }

    public IJSpace getIJSpace() {
        return this.ijspace;
    }

    public IInternalRemoteJSpaceAdmin getSpaceAdmin() {
        return this.spaceAdmin;
    }

    public void setMode(SpaceMode spaceMode) {
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

    private static final SpaceInstanceStatistics NA_STATISTICS = new DefaultSpaceInstanceStatistics(new StatisticsHolder(new long[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}), null, 0);

    public synchronized SpaceInstanceStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        try {
            lastStatistics = new DefaultSpaceInstanceStatistics(((StatisticsAdmin) spaceAdmin).getHolder(), lastStatistics, statisticsHistorySize);
        } catch (RemoteException e) {
            lastStatistics = NA_STATISTICS;
        }
        return lastStatistics;
    }

    public ReplicationTarget[] getReplicationTargets() {
        return replicationTargets;
    }

    public void setReplicationTargets(ReplicationTarget[] replicationTargets) {
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
                ReplicationTarget previousReplicationTarget = previousReplicationTargets[i];
                if (newReplicationTarget.getReplicationStatus() != previousReplicationTarget.getReplicationStatus()) {
                    events.add(new ReplicationStatusChangedEvent(this, newReplicationTarget, previousReplicationTarget.getReplicationStatus(), newReplicationTarget.getReplicationStatus()));
                }
            }
        }
        for (ReplicationStatusChangedEvent event : events) {
            replicationStatusChangedEventManager.replicationStatusChanged(event);
            ((InternalReplicationStatusChangedEventManager) getSpace().getReplicationStatusChanged()).replicationStatusChanged(event);
            ((InternalReplicationStatusChangedEventManager) getSpace().getSpaces().getReplicationStatusChanged()).replicationStatusChanged(event);
        }
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public void setPartition(SpacePartition spacePartition) {
        this.spacePartition = spacePartition;
    }

    public SpacePartition getPartition() {
        return this.spacePartition;
    }

    public NIODetails getNIODetails() throws RemoteException {
        return spaceAdmin.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return spaceAdmin.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return spaceAdmin.getOSDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return spaceAdmin.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return spaceAdmin.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return spaceAdmin.getJVMStatistics();
    }

    public void runGc() throws RemoteException {
        spaceAdmin.runGc();
    }
}
