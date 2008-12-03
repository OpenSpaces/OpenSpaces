package org.openspaces.admin.internal.space;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.DefaultSpaceAddedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceRemovedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceAddedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceRemovedEventManager;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceAddedEventManager;
import org.openspaces.admin.space.events.SpaceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceRemovedEventManager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultSpaces implements InternalSpaces {

    private final InternalAdmin admin;

    private final Map<String, Space> spacesByUID = new SizeConcurrentHashMap<String, Space>();

    private final Map<String, Space> spacesByName = new ConcurrentHashMap<String, Space>();

    private final Map<String, SpaceInstance> spacesInstances = new ConcurrentHashMap<String, SpaceInstance>();

    private final InternalSpaceAddedEventManager spaceAddedEventManager;

    private final InternalSpaceRemovedEventManager spaceRemovedEventManager;

    public DefaultSpaces(InternalAdmin admin) {
        this.admin = admin;
        this.spaceAddedEventManager = new DefaultSpaceAddedEventManager(this);
        this.spaceRemovedEventManager = new DefaultSpaceRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public Space[] getSpaces() {
        return spacesByUID.values().toArray(new Space[0]);
    }

    public Space getSpaceByUID(String uid) {
        return spacesByUID.get(uid);
    }

    public Space getSpaceByName(String name) {
        return spacesByName.get(name);
    }

    public Iterator<Space> iterator() {
        return spacesByUID.values().iterator();
    }

    public SpaceAddedEventManager getSpaceAdded() {
        return this.spaceAddedEventManager;
    }

    public SpaceRemovedEventManager getSpaceRemoved() {
        return this.spaceRemovedEventManager;
    }

    public void addLifecycleListener(SpaceLifecycleEventListener eventListener) {
        getSpaceAdded().add(eventListener);
        getSpaceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(SpaceLifecycleEventListener eventListener) {
        getSpaceAdded().remove(eventListener);
        getSpaceRemoved().remove(eventListener);
    }

    public synchronized void addSpace(Space space) {
        Space existingSpace = spacesByUID.put(space.getUid(), space);
        spacesByName.put(space.getName(), space);
        if (existingSpace == null) {
            spaceAddedEventManager.spaceAdded(space);
        }
    }

    public synchronized InternalSpace removeSpace(String uid) {
        Space space = spacesByUID.remove(uid);
        if (space != null) {
            spacesByName.remove(space.getName());
            spaceRemovedEventManager.spaceRemoved(space);
        }
        return (InternalSpace) space;
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spacesInstances.put(spaceInstance.getUid(), spaceInstance);
    }

    public SpaceInstance removeSpaceInstance(String uid) {
        return spacesInstances.remove(uid);
    }
}
