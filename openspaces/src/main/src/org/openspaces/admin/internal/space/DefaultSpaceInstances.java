package org.openspaces.admin.internal.space;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceAddedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceRemovedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceAddedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultSpaceInstances implements InternalSpaceInstances {

    private final InternalAdmin admin;

    private final Map<String, SpaceInstance> spaceInstances = new SizeConcurrentHashMap<String, SpaceInstance>();

    private final InternalSpaceInstanceAddedEventManager spaceInstanceAddedEventManager;

    private final InternalSpaceInstanceRemovedEventManager spaceInstanceRemovedEventManager;

    public DefaultSpaceInstances(InternalAdmin admin) {
        this.admin = admin;
        this.spaceInstanceAddedEventManager = new DefaultSpaceInstanceAddedEventManager(admin, this);
        this.spaceInstanceRemovedEventManager = new DefaultSpaceInstanceRemovedEventManager(admin);
    }

    public boolean contains(SpaceInstance spaceInstance) {
        for (SpaceInstance it : spaceInstances.values()) {
            if (it.getUid().equals(spaceInstance.getUid())) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return spaceInstances.size();
    }

    public Iterator<SpaceInstance> iterator() {
        return spaceInstances.values().iterator();
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.values().toArray(new SpaceInstance[0]);
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        SpaceInstance existing = spaceInstances.put(spaceInstance.getUid(), spaceInstance);
        if (existing == null) {
            spaceInstanceAddedEventManager.spaceInstanceAdded(spaceInstance);
        }
    }

    public void removeSpaceInstance(String uid) {
        SpaceInstance existing = spaceInstances.remove(uid);
        if (existing != null) {
            spaceInstanceRemovedEventManager.spaceInstanceRemoved(existing);
        }
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
}
