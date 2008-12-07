package org.openspaces.admin.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEventManager;
import org.openspaces.core.GigaSpace;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface Space extends Iterable<SpaceInstance>, StatisticsMonitor {

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

    /**
     * Returns the clustered view of the space to operate on.
     */
    GigaSpace getGigaSpace();

    /**
     * Waits till at least the provided number of Space Instances are up.
     */
    boolean waitFor(int numberOfSpaceInstances);

    /**
     * Waits till at least the provided number of Space Instances are up for the specified timeout.
     */
    boolean waitFor(int numberOfSpaceInstances, long timeout, TimeUnit timeUnit);

    /**
     * Waits till at least the provided number of Space Instances that are of the space mode typw are up.
     */
    boolean waitFor(int numberOfSpaceInstances, SpaceMode spaceMode);

    /**
     * Waits till at least the provided number of Space Instances are of the space mode type are up for the specified timeout.
     */
    boolean waitFor(int numberOfSpaceInstances, SpaceMode spaceMode, long timeout, TimeUnit timeUnit);

    /**
     * Returns an aggregated view of all the statistics of all the instances.
     */
    SpaceStatistics getStatistics();

    /**
     * Returns an aggregated view of all the statistics of the primary instances.
     */
    SpaceStatistics getPrimariesStatistics();

    /**
     * Returns an aggregated view of all the statistics of the backup instances.
     */
    SpaceStatistics getBackupsStatistics();

    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    SpaceModeChangedEventManager getSpaceModeChanged();

    ReplicationStatusChangedEventManager getReplicationStatusChanged();

    SpaceStatisticsChangedEventManager getStatisticsChanged();

    SpaceInstanceStatisticsChangedEventManager getInstanceStatisticsChanged();
}

