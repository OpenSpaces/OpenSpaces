/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * A space is composed of several {@link org.openspaces.admin.space.SpaceInstance}s the form a
 * topology.
 *
 * <p>There are two main logical of topologies, one that has backups to each space instance, and
 * one that is without backups. For example, a partitioned topology of 2 partitions, each with one
 * backup will return 2 for {@link #getNumberOfInstances()} and 1 for {@link #getNumberOfBackups()}.
 * A replicated topology of 4 will return 4 for {@link #getNumberOfInstances()} and 0 for
 * {@link #getNumberOfBackups()}.
 *
 * <p>A Space will be discovered once one of its {@link org.openspaces.admin.space.SpaceInstance}
 * have been discovered. It will be removed once there are no {@link org.openspaces.admin.space.SpaceInstance}s
 * running.
 *
 * <p>Provides the ability to start a statistics monitor on all current {@link org.openspaces.admin.space.SpaceInstance}s using
 * {@link #startStatisticsMonitor()}. Newly discovered space instances will automatically use
 * the statistics monitor as well.
 *
 * @author kimchy
 */
public interface Space extends Iterable<SpaceInstance>, StatisticsMonitor {

    /**
     * Returns the spaces this space is one of.
     */
    Spaces getSpaces();

    /**
     * Returns the uid of the Space.
     */
    String getUid();

    /**
     * Returns the name of the Space.
     */
    String getName();

    /**
     * Returns the number of instances as per the Space topology. Will return 4 if we have a replicated
     * topology, and 2 if we have a 2 partitions each with one backup topology.
     */
    int getNumberOfInstances();

    /**
     * Returns the number of backups per Space Instance. Returns 1 when we deploy a 2 partitions each with
     * one backup topology.
     */
    int getNumberOfBackups();

    /**
     * Returns the total number of instances. If there are no backups, will return
     * {@link #getNumberOfInstances()}. If there are backups, will return {@link #getNumberOfInstances()} * ({@link #getNumberOfBackups()}  + 1)
     */
    int getTotalNumberOfInstances();
    
    /**
     * Returns all the space instances that are currently discovered that are part of this Space topology.
     */
    SpaceInstance[] getInstances();

    /**
     * Returns all the partitions that form this Space topology.
     */
    SpacePartition[] getPartitions();

    /**
     * Returns a partition for a specific partition id.
     */
    SpacePartition getPartition(int partitionId);

    /**
     * Returns the number of currently discovered space instances.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are currently no space instances discovered.
     */
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
     * Waits till at least the provided number of Space Instances that are of the space mode type are up.
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
    
    /**
     * Returns an aggregated view of all the Space runtime details of all primary instances.
     */
    SpaceRuntimeDetails getRuntimeDetails();

    /**
     * Allows to registered {@link org.openspaces.admin.space.events.SpaceInstanceAddedEventListener} to be notified
     * when space instances are added.
     */
    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    /**
     * Allows to registered {@link org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener} to be notified
     * when space instances are removed.
     */
    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    /**
     * Allows to add {@link org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener}.
     */
    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove {@link org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener}.
     */
    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Allows to globally regsiter for each {@link org.openspaces.admin.space.SpaceInstance}
     * {@link org.openspaces.admin.space.events.SpaceModeChangedEvent}.
     */
    SpaceModeChangedEventManager getSpaceModeChanged();

    /**
     * Allows to globally register for each {@link org.openspaces.admin.space.SpaceInstance}
     * {@link org.openspaces.admin.space.events.ReplicationStatusChangedEvent}.
     */
    ReplicationStatusChangedEventManager getReplicationStatusChanged();

    /**
     * Allows to register for aggregated Space level statistics {@link org.openspaces.admin.space.events.SpaceStatisticsChangedEvent}.
     *
     * <p>Note, statistics monitoring must be started using {@link #startStatisticsMonitor()} in order to receive
     * events.
     */
    SpaceStatisticsChangedEventManager getStatisticsChanged();

    /**
     * Allows to register for space instance level statistics {@link org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent}s.
     *
     * <p>Note, statistics monitoring must be started using {@link #startStatisticsMonitor()} in order to receive
     * events.
     */
    SpaceInstanceStatisticsChangedEventManager getInstanceStatisticsChanged();
}

