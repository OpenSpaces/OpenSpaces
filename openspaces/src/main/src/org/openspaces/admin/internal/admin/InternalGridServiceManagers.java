package org.openspaces.admin.internal.admin;

import org.openspaces.admin.GridServiceManagers;

/**
 * @author kimchy
 */
public interface InternalGridServiceManagers extends GridServiceManagers {

    void addGridServiceManager(InternalGridServiceManager gridServiceManager);

    InternalGridServiceManager removeGridServiceManager(String uid);

    /**
     * Replaces the grid service manager, returning the old one
     */
    InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager);
}