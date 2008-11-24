package org.openspaces.admin.internal.gsc;

import org.openspaces.admin.gsc.GridServiceContainers;

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