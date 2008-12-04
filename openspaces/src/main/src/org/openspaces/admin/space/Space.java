package org.openspaces.admin.space;

import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;

/**
 * @author kimchy
 */
public interface Space extends Iterable<SpaceInstance> {

    Spaces getSpaces();

    String getUid();

    String getName();

    int getNumberOfInstances();

    int getNumberOfBackups();

    SpaceInstance[] getInstnaces();

    SpacePartition[] getPartitions();

    SpacePartition getPartition(int partitionId);

    int getSize();

    boolean isEmpty();

    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    SpaceModeChangedEventManager getSpaceModeChanged();

    ReplicationStatusChangedEventManager getReplicationStatusChanged();
}

