package org.openspaces.admin.gsc;

/**
 * @author kimchy
 */
public interface GridServiceContainerEventListener {

    void gridServiceContainerAdded(GridServiceContainer gridServiceContainer);

    void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer);
}
