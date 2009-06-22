package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;

/**
 * @author kimchy
 */
public interface InternalSpaceInstances extends Iterable<SpaceInstance>, InternalSpaceInstancesAware {

    boolean contains(SpaceInstance spaceInstance);

    int size();

    void addSpaceInstance(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);

    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);
}
