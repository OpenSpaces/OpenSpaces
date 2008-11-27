package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface SpaceInstance {

    String getUID();

    int getInstanceId();

    int getBackupId();

    Space getSpace();
}
