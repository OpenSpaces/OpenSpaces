package org.openspaces.admin.internal.space;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultSpace implements InternalSpace {

    private final String uid;

    private final String name;

    private final Map<String, SpaceInstance> spaceInstances = new SizeConcurrentHashMap<String, SpaceInstance>();

    private final Map<Integer, SpacePartition> spacePartitions = new SizeConcurrentHashMap<Integer, SpacePartition>();

    public DefaultSpace(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public String getUID() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public int getNumberOfInstances() {
        for (SpaceInstance spaceInstance : spaceInstances.values()) {
            return ((InternalSpaceInstance) spaceInstance).getNumberOfInstances();
        }
        return -1;
    }

    public int getNumberOfBackups() {
        for (SpaceInstance spaceInstance : spaceInstances.values()) {
            return ((InternalSpaceInstance) spaceInstance).getNumberOfBackups();
        }
        return -1;
    }

    public SpaceInstance[] getInstnaces() {
        return spaceInstances.values().toArray(new SpaceInstance[0]);
    }

    public Iterator<SpaceInstance> iterator() {
        return spaceInstances.values().iterator();
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
        if (spaceInstances.size() == 0) {
            for (int i = 0; i < internalSpaceInstance.getNumberOfInstances(); i++) {
                spacePartitions.put(i, new DefaultSpacePartition(this, i));
            }
        }
        spaceInstances.put(spaceInstance.getUID(), spaceInstance);
        InternalSpacePartition spacePartition = (InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1);
        internalSpaceInstance.setPartition(spacePartition);
        spacePartition.addSpaceInstance(spaceInstance);
    }

    public InternalSpaceInstance removeInstance(String uid) {
        InternalSpaceInstance spaceInstance = (InternalSpaceInstance) spaceInstances.remove(uid);
        ((InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1)).removeSpaceInstance(uid);
        return spaceInstance;
    }

    public int size() {
        return spaceInstances.size();
    }
}
