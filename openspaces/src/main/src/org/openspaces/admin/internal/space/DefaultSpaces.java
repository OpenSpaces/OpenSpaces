package org.openspaces.admin.internal.space;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultSpaces implements InternalSpaces {

    private final Map<String, Space> spacesByUID = new SizeConcurrentHashMap<String, Space>();

    private final Map<String, Space> spacesByName = new ConcurrentHashMap<String, Space>();

    private final Map<String, SpaceInstance> spacesInstances = new ConcurrentHashMap<String, SpaceInstance>();

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

    public synchronized void addSpace(Space space) {
        spacesByUID.put(space.getUID(), space);
        spacesByName.put(space.getName(), space);
    }

    public synchronized InternalSpace removeSpace(String uid) {
        Space space = spacesByUID.remove(uid);
        if (space != null) {
            spacesByName.remove(space.getName());
        }
        return (InternalSpace) space;
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spacesInstances.put(spaceInstance.getUID(), spaceInstance);
    }

    public SpaceInstance removeSpaceInstance(String uid) {
        return spacesInstances.remove(uid);
    }
}
