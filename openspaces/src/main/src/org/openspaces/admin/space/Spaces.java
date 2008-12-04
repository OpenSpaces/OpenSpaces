package org.openspaces.admin.space;

import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.space.events.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface Spaces extends Iterable<Space>, StatisticsMonitor {

    Admin getAdmin();

    Space[] getSpaces();

    Space getSpaceByUID(String uid);

    Space getSpaceByName(String name);

    Map<String, Space> getNames();

    Space waitFor(String spaceName);

    Space waitFor(String spaceName, long timeout, TimeUnit timeUnit);

    void addLifecycleListener(SpaceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceLifecycleEventListener eventListener);

    SpaceAddedEventManager getSpaceAdded();

    SpaceRemovedEventManager getSpaceRemoved();

    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    SpaceModeChangedEventManager getSpaceModeChanged();

    ReplicationStatusChangedEventManager getReplicationStatusChanged();

    SpaceStatisticsChangedEventManager getSpaceStatisticsChanged();

    SpaceInstanceStatisticsChangedEventManager getSpaceInstanceStatisticsChanged();
}
