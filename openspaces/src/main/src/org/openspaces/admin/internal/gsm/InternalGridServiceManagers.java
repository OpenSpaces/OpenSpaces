package org.openspaces.admin.internal.gsm;

import org.openspaces.admin.gsm.GridServiceManagers;

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