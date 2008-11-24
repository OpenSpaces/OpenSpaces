package org.openspaces.admin.internal.admin;

import org.openspaces.admin.GridServiceContainers;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainers extends GridServiceContainers {

    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer);

    InternalGridServiceContainer removeGridServiceContainer(String uid);

    /**
     * Replaces the grid service container, returning the old one
     */
    InternalGridServiceContainer replaceGridServiceContainer(InternalGridServiceContainer gridServiceContainer);
}