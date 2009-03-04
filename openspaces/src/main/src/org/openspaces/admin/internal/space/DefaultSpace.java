package org.openspaces.admin.internal.space;

import com.gigaspaces.cluster.activeelection.InactiveSpaceException;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.exception.SpaceUnavailableException;
import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.*;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.space.SpaceStatistics;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.space.events.*;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultSpace implements InternalSpace {

    private static final Log logger = LogFactory.getLog(DefaultSpace.class);

    private final InternalAdmin admin;

    private final InternalSpaces spaces;

    private final String uid;

    private final String name;

    private final IJSpace space;

    private final GigaSpace gigaSpace;

    private final Map<String, SpaceInstance> spaceInstancesByUID = new SizeConcurrentHashMap<String, SpaceInstance>();

    private final Map<String, SpaceInstance> spaceInstancesByMemberName = new ConcurrentHashMap<String, SpaceInstance>();

    private final Map<Integer, SpacePartition> spacePartitions = new SizeConcurrentHashMap<Integer, SpacePartition>();

    private final Map<String, Future> scheduledRuntimeFetchers = new ConcurrentHashMap<String, Future>();

    private final InternalSpaceInstanceAddedEventManager spaceInstanceAddedEventManager;

    private final InternalSpaceInstanceRemovedEventManager spaceInstanceRemovedEventManager;

    private final InternalSpaceModeChangedEventManager spaceModeChangedEventManager;

    private final InternalReplicationStatusChangedEventManager replicationStatusChangedEventManager;

    private final InternalSpaceStatisticsChangedEventManager statisticsChangedEventManager;

    private final InternalSpaceInstanceStatisticsChangedEventManager instanceStatisticsChangedEventManager;

    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private long lastStatisticsTimestamp = 0;

    private SpaceStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;

    public DefaultSpace(InternalSpaces spaces, String uid, String name, IJSpace clusteredSpace) {
        this.spaces = spaces;
        this.admin = (InternalAdmin) spaces.getAdmin();
        this.uid = uid;
        this.name = name;

        this.space = clusteredSpace;
        this.gigaSpace = new GigaSpaceConfigurer(space).clustered(true).gigaSpace();
        
        this.spaceInstanceAddedEventManager = new DefaultSpaceInstanceAddedEventManager(admin, this);
        this.spaceInstanceRemovedEventManager = new DefaultSpaceInstanceRemovedEventManager(admin);
        this.spaceModeChangedEventManager = new DefaultSpaceModeChangedEventManager(this, admin);
        this.replicationStatusChangedEventManager = new DefaultReplicationStatusChangedEventManager(admin);
        this.statisticsChangedEventManager = new DefaultSpaceStatisticsChangedEventManager(admin);
        this.instanceStatisticsChangedEventManager = new DefaultSpaceInstanceStatisticsChangedEventManager(admin);
    }

    public Spaces getSpaces() {
        return this.spaces;
    }

    public String getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        if (isMonitoring()) {
            rescheduleStatisticsMonitor();
        }
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.setStatisticsInterval(interval, timeUnit);
        }
    }

    public synchronized void startStatisticsMonitor() {
        rescheduleStatisticsMonitor();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.startStatisticsMonitor();
        }
    }

    public synchronized void stopStatisticsMontior() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.stopStatisticsMontior();
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    private void rescheduleStatisticsMonitor() {
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        scheduledStatisticsMonitor = admin.getScheduler().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                SpaceStatistics stats = getStatistics();
                SpaceStatisticsChangedEvent event = new SpaceStatisticsChangedEvent(DefaultSpace.this, stats);
                statisticsChangedEventManager.spaceStatisticsChanged(event);
                ((InternalSpaceStatisticsChangedEventManager) spaces.getSpaceStatisticsChanged()).spaceStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public int getNumberOfInstances() {
        return doWithInstance(new InstanceCallback<Integer>() {
            public Integer doWithinstance(InternalSpaceInstance spaceInstance) {
                return spaceInstance.getNumberOfInstances();
            }
        }, -1);
    }

    public int getNumberOfBackups() {
        return doWithInstance(new InstanceCallback<Integer>() {
            public Integer doWithinstance(InternalSpaceInstance spaceInstance) {
                return spaceInstance.getNumberOfBackups();
            }
        }, -1);
    }

    public SpaceInstance[] getInstances() {
        return spaceInstancesByUID.values().toArray(new SpaceInstance[0]);
    }

    public Iterator<SpaceInstance> iterator() {
        return spaceInstancesByUID.values().iterator();
    }

    public SpacePartition[] getPartitions() {
        return spacePartitions.values().toArray(new SpacePartition[0]);
    }

    public SpacePartition getPartition(int partitionId) {
        return spacePartitions.get(partitionId);
    }

    public SpaceModeChangedEventManager getSpaceModeChanged() {
        return this.spaceModeChangedEventManager;
    }

    public ReplicationStatusChangedEventManager getReplicationStatusChanged() {
        return this.replicationStatusChangedEventManager;
    }

    public SpaceStatisticsChangedEventManager getStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }

    public SpaceInstanceStatisticsChangedEventManager getInstanceStatisticsChanged() {
        return this.instanceStatisticsChangedEventManager;
    }

    public synchronized SpaceStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        List<SpaceInstanceStatistics> stats = new ArrayList<SpaceInstanceStatistics>();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            stats.add(spaceInstance.getStatistics());
        }
        lastStatistics = new DefaultSpaceStatistics(stats.toArray(new SpaceInstanceStatistics[stats.size()]));
        return lastStatistics;
    }

    public SpaceStatistics getPrimariesStatistics() {
        List<SpaceInstanceStatistics> stats = new ArrayList<SpaceInstanceStatistics>();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                stats.add(spaceInstance.getStatistics());
            }
        }
        return new DefaultSpaceStatistics(stats.toArray(new SpaceInstanceStatistics[stats.size()]));
    }

    public SpaceStatistics getBackupsStatistics() {
        List<SpaceInstanceStatistics> stats = new ArrayList<SpaceInstanceStatistics>();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            if (spaceInstance.getMode() == SpaceMode.BACKUP) {
                stats.add(spaceInstance.getStatistics());
            }
        }
        return new DefaultSpaceStatistics(stats.toArray(new SpaceInstanceStatistics[stats.size()]));
    }

    public void addInstance(SpaceInstance spaceInstance) {
        InternalSpaceInstance internalSpaceInstance = (InternalSpaceInstance) spaceInstance;
        // the first addition (which we make sure is added before the space becomes visible) will
        // cause the parition to initialize
        if (spaceInstancesByUID.size() == 0) {
            if (internalSpaceInstance.getNumberOfBackups() == 0) {
                // single patition when there is no backup
                spacePartitions.put(0, new DefaultSpacePartition(this, 0));
            } else {
                for (int i = 0; i < internalSpaceInstance.getNumberOfInstances(); i++) {
                    spacePartitions.put(i, new DefaultSpacePartition(this, i));
                }
            }
        }
        SpaceInstance existing = spaceInstancesByUID.put(spaceInstance.getUid(), spaceInstance);
        spaceInstancesByMemberName.put(((InternalSpaceInstance) spaceInstance).getSpaceConfig().getFullSpaceName(), spaceInstance);
        InternalSpacePartition spacePartition = getPartition(internalSpaceInstance);
        internalSpaceInstance.setPartition(spacePartition);
        spacePartition.addSpaceInstance(spaceInstance);

        if (existing == null) {
            spaceInstanceAddedEventManager.spaceInstanceAdded(spaceInstance);
            ((InternalSpaceInstanceAddedEventManager) spaces.getSpaceInstanceAdded()).spaceInstanceAdded(spaceInstance);
        }

        spaceInstance.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
        if (isMonitoring()) {
            spaceInstance.startStatisticsMonitor();
        }

        // start the scheduler
        Future fetcher = scheduledRuntimeFetchers.get(spaceInstance.getUid());
        if (fetcher == null) {
            fetcher = admin.getScheduler().scheduleWithFixedDelay(new ScheduledRuntimeFetcher(spaceInstance), admin.getScheduledSpaceMonitorInterval(), admin.getScheduledSpaceMonitorInterval(), TimeUnit.MILLISECONDS);
            scheduledRuntimeFetchers.put(spaceInstance.getUid(), fetcher);
        }
    }

    public InternalSpaceInstance removeInstance(String uid) {
        InternalSpaceInstance spaceInstance = (InternalSpaceInstance) spaceInstancesByUID.remove(uid);
        if (spaceInstance != null) {
            spaceInstance.stopStatisticsMontior();
            getPartition(spaceInstance).removeSpaceInstance(uid);
            spaceInstanceRemovedEventManager.spaceInstanceRemoved(spaceInstance);
            ((InternalSpaceInstanceRemovedEventManager) spaces.getSpaceInstanceRemoved()).spaceInstanceRemoved(spaceInstance);
            Future fetcher = scheduledRuntimeFetchers.get(uid);
            if (fetcher != null) {
                fetcher.cancel(true);
            }
        }
        return spaceInstance;
    }

    private InternalSpacePartition getPartition(InternalSpaceInstance spaceInstance) {
        if (spaceInstance.getNumberOfBackups() == 0) {
            return (InternalSpacePartition) spacePartitions.get(0);
        } else {
            return (InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1);
        }
    }

    // no need to sync since it is synced on Admin
    public void refreshScheduledSpaceMonitors() {
        for (Future fetcher : scheduledRuntimeFetchers.values()) {
            fetcher.cancel(false);
        }
        for (SpaceInstance spaceInstance : this) {
            Future fetcher = admin.getScheduler().scheduleWithFixedDelay(new ScheduledRuntimeFetcher(spaceInstance), admin.getScheduledSpaceMonitorInterval(), admin.getScheduledSpaceMonitorInterval(), TimeUnit.MILLISECONDS);
            scheduledRuntimeFetchers.put(spaceInstance.getUid(), fetcher);
        }
    }

    public int getSize() {
        return spaceInstancesByUID.size();
    }

    public boolean isEmpty() {
        return spaceInstancesByUID.size() == 0;
    }

    public GigaSpace getGigaSpace() {
        return this.gigaSpace;
    }

    public boolean waitFor(int numberOfSpaceInstances) {
        return waitFor(numberOfSpaceInstances, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitFor(int numberOfSpaceInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfSpaceInstances);
        SpaceInstanceAddedEventListener added = new SpaceInstanceAddedEventListener() {
            public void spaceInstanceAdded(SpaceInstance spaceInstance) {
                latch.countDown();
            }
        };
        getSpaceInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getSpaceInstanceAdded().remove(added);
        }
    }

    public boolean waitFor(int numberOfSpaceInstances, SpaceMode spaceMode) {
        return waitFor(numberOfSpaceInstances, spaceMode, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitFor(int numberOfSpaceInstances, final SpaceMode spaceMode, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfSpaceInstances);
        SpaceModeChangedEventListener modeChanged = new SpaceModeChangedEventListener() {
            public void spaceModeChanged(SpaceModeChangedEvent event) {
                if (event.getNewMode() == spaceMode) {
                    latch.countDown();
                }
            }
        };
        getSpaceModeChanged().add(modeChanged);
        boolean result;
        try {
            result = latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getSpaceModeChanged().remove(modeChanged);
        }
        if (result) {
            // double check again
            int sum = 0;
            for (SpaceInstance spaceInstance : this) {
                if (spaceInstance.getMode() == spaceMode) {
                    sum++;
                }
            }
            if (sum < numberOfSpaceInstances) {
                return waitFor(numberOfSpaceInstances, spaceMode, timeout, timeUnit);
            }
        }
        return result;
    }

    public SpaceInstance[] getSpaceInstances() {
        return getInstances();
    }

    public SpaceInstanceAddedEventManager getSpaceInstanceAdded() {
        return this.spaceInstanceAddedEventManager;
    }

    public SpaceInstanceRemovedEventManager getSpaceInstanceRemoved() {
        return this.spaceInstanceRemovedEventManager;
    }

    public void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstanceAddedEventManager.add(eventListener);
        spaceInstanceRemovedEventManager.add(eventListener);
    }

    public void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstanceAddedEventManager.remove(eventListener);
        spaceInstanceRemovedEventManager.remove(eventListener);
    }

    private <T> T doWithInstance(InstanceCallback<T> callback, T naValue) {
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            return callback.doWithinstance((InternalSpaceInstance) spaceInstance);
        }
        return naValue;
    }

    private static interface InstanceCallback<T> {
        T doWithinstance(InternalSpaceInstance spaceInstance);
    }

    private class ScheduledRuntimeFetcher implements Runnable {

        private final InternalSpaceInstance spaceInstance;

        private ScheduledRuntimeFetcher(SpaceInstance spaceInstance) {
            this.spaceInstance = (InternalSpaceInstance) spaceInstance;
        }

        public void run() {
            try {
                RuntimeHolder runtimeHolder = spaceInstance.getSpaceAdmin().getRuntimeHolder();
                spaceInstance.setMode(runtimeHolder.getSpaceMode());
                if (runtimeHolder.getReplicationStatus() != null) {
                    Object[] memberNames = (Object[]) runtimeHolder.getReplicationStatus()[0];
                    int[] replicationStatus = (int[]) runtimeHolder.getReplicationStatus()[1];
                    ReplicationTarget[] replicationTargets = new ReplicationTarget[memberNames.length];
                    for (int i = 0; i < memberNames.length; i++) {
                        if (memberNames[i] == null) {
                            continue;
                        }
                        SpaceInstance targetSpaceInstance = spaceInstancesByMemberName.get((String) memberNames[i]);
                        ReplicationStatus replStatus = null;
                        switch (replicationStatus[i]) {
                            case IRemoteJSpaceAdmin.REPLICATION_STATUS_ACTIVE:
                                replStatus = ReplicationStatus.ACTIVE;
                                break;
                            case IRemoteJSpaceAdmin.REPLICATION_STATUS_DISCONNECTED:
                                replStatus = ReplicationStatus.DISCONNECTED;
                                break;
                            case IRemoteJSpaceAdmin.REPLICATION_STATUS_DISABLED:
                                replStatus = ReplicationStatus.DISABLED;
                                break;
                        }
                        replicationTargets[i] = new ReplicationTarget((InternalSpaceInstance) targetSpaceInstance, replStatus);
                    }
                    spaceInstance.setReplicationTargets(replicationTargets);
                }
            } catch (SpaceUnavailableException e) {
                // space is going shutdown or abort process
            } catch (InactiveSpaceException e) {
                // ignore this (maybe we should add it as a state to a Space instnace?)
            } catch (Exception e) {
                if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                    // Space is down, ignore
                } else {
                    logger.warn("Failed to get runtime information", e);
                }
            }
        }
    }
}
