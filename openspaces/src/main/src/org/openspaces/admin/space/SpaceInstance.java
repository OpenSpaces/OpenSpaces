package org.openspaces.admin.space;

import org.openspaces.admin.GridComponent;

/**
 * @author kimchy
 */
public interface SpaceInstance extends GridComponent {

    int getInstanceId();

    int getBackupId();

    Space getSpace();
}
