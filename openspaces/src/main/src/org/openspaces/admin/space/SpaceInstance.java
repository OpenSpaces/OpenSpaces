package org.openspaces.admin.space;

import org.openspaces.admin.GridComponent;

/**
 * @author kimchy
 */
public interface SpaceInstance extends GridComponent {

    /**
     * Returns the instance id of the space (starting from 1).
     */
    int getInstanceId();

    int getBackupId();

    Space getSpace();

    SpacePartition getPartition();
}
