package org.openspaces.admin.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;

/**
 * @author kimchy
 */
public interface SpaceInstance extends GridComponent, StatisticsMonitor {

    /**
     * Returns the instance id of the space (starting from 1).
     */
    int getInstanceId();

    int getBackupId();

    SpaceMode getMode();

    SpaceInstanceStatistics getStatistics();

    Space getSpace();

    SpacePartition getPartition();

    ReplicationTarget[] getReplicationTargets();

    SpaceModeChangedEventManager getSpaceModeChanged();

    ReplicationStatusChangedEventManager getReplicationStatusChanged();

    SpaceInstanceStatisticsChangedEventManager getStatisticsChanged();
}
