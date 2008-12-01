package org.openspaces.admin.gsc.events;

import org.openspaces.admin.gsc.GridServiceContainer;

/**
 * @author kimchy
 */
public interface GridServiceContainerAddedEventListener {

    void gridServiceContainerAdded(GridServiceContainer gridServiceContainer);
}