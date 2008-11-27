package org.openspaces.admin.internal.space;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.space.SpaceInstance;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultSpace implements InternalSpace {

    private final String uid;

    private final String name;

    private final Map<String, SpaceInstance> spaceInstances = new SizeConcurrentHashMap<String, SpaceInstance>();

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

    public void addInstance(SpaceInstance spaceInstance) {
        spaceInstances.put(spaceInstance.getUID(), spaceInstance);
    }

    public InternalSpaceInstance removeInstance(String uid) {
        return (InternalSpaceInstance) spaceInstances.remove(uid);
    }

    public int size() {
        return spaceInstances.size();
    }
}
