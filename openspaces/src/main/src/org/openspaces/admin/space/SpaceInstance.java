package org.openspaces.admin.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;

/**
 * @author kimchy
 */
public interface SpaceInstance extends GridComponent {

    /**
     * Returns the instance id of the space (starting from 1).
     */
    int getInstanceId();

    int getBackupId();

    SpaceMode getMode();

    Space getSpace();

    SpacePartition getPartition();

    ReplicationTarget[] getReplicationTargets();

    SpaceModeChangedEventManager getSpaceModeChanged();

    ReplicationStatusChangedEventManager getReplicationStatusChanged();
}
