package org.openspaces.admin.gsc.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsc.GridServiceContainer;

/**
 * @author kimchy
 */
public interface GridServiceContainerAddedEventListener extends AdminEventListener {

    void gridServiceContainerAdded(GridServiceContainer gridServiceContainer);
}