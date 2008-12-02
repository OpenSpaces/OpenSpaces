package org.openspaces.admin.gsc.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsc.GridServiceContainer;

/**
 * @author kimchy
 */
public interface GridServiceContainerRemovedEventListener extends AdminEventListener {

    void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer);
}