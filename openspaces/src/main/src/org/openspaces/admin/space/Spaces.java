package org.openspaces.admin.space;

import org.openspaces.admin.Admin;
import org.openspaces.admin.space.events.SpaceAddedEventManager;
import org.openspaces.admin.space.events.SpaceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceRemovedEventManager;

/**
 * @author kimchy
 */
public interface Spaces extends Iterable<Space> {

    Admin getAdmin();

    Space[] getSpaces();

    Space getSpaceByUID(String uid);

    Space getSpaceByName(String name);

    void addLifecycleListener(SpaceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceLifecycleEventListener eventListener);

    SpaceAddedEventManager getSpaceAdded();

    SpaceRemovedEventManager getSpaceRemoved();
}
