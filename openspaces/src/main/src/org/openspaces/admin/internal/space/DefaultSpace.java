package org.openspaces.admin.internal.space;

import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultSpace implements InternalSpace {

    private static final Log logger = LogFactory.getLog(DefaultSpace.class);

    private final InternalAdmin admin;

    private final String uid;

    private final String name;

    private final Map<String, SpaceInstance> spaceInstancesByUID = new SizeConcurrentHashMap<String, SpaceInstance>();

    private final Map<String, SpaceInstance> spaceInstancesByMemberName = new ConcurrentHashMap<String, SpaceInstance>();

    private final Map<Integer, SpacePartition> spacePartitions = new SizeConcurrentHashMap<Integer, SpacePartition>();

    private final Map<String, Future> scheduledRuntimeFetchers = new ConcurrentHashMap<String, Future>();

    public DefaultSpace(InternalAdmin admin, String uid, String name) {
        this.admin = admin;
        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
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

    public SpaceInstance[] getInstnaces() {
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

    public void addInstance(SpaceInstance spaceInstance) {
        InternalSpaceInstance internalSpaceInstance = (InternalSpaceInstance) spaceInstance;
        // the first addition (which we make sure is added before the space becomes visible) will
        // cause the parition to initialize
        if (spaceInstancesByUID.size() == 0) {
            for (int i = 0; i < internalSpaceInstance.getNumberOfInstances(); i++) {
                spacePartitions.put(i, new DefaultSpacePartition(this, i));
            }
        }
        spaceInstancesByUID.put(spaceInstance.getUid(), spaceInstance);
        spaceInstancesByMemberName.put(((InternalSpaceInstance) spaceInstance).getSpaceConfig().getFullSpaceName(), spaceInstance);
        InternalSpacePartition spacePartition = (InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1);
        internalSpaceInstance.setPartition(spacePartition);
        spacePartition.addSpaceInstance(spaceInstance);

        // start the scheduler
        Future fetcher = scheduledRuntimeFetchers.get(spaceInstance.getUid());
        if (fetcher == null) {
            // TODO make the schedule configurable
            fetcher = admin.getScheduler().scheduleWithFixedDelay(new ScheduledRuntimeFetcher(spaceInstance), 1000, 1000, TimeUnit.MILLISECONDS);
            scheduledRuntimeFetchers.put(spaceInstance.getUid(), fetcher);
        }
    }

    public InternalSpaceInstance removeInstance(String uid) {
        InternalSpaceInstance spaceInstance = (InternalSpaceInstance) spaceInstancesByUID.remove(uid);
        ((InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1)).removeSpaceInstance(uid);
        if (spaceInstance != null) {
            Future fetcher = scheduledRuntimeFetchers.get(uid);
            if (fetcher != null) {
                fetcher.cancel(true);
            }
        }
        return spaceInstance;
    }

    public int getSize() {
        return spaceInstancesByUID.size();
    }

    public boolean isEmpty() {
        return spaceInstancesByUID.size() == 0;
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
