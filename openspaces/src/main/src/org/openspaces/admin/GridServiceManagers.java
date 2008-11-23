package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface GridServiceManagers extends Iterable<GridServiceManager> {

    GridServiceManager[] getManagers();

    GridServiceManager getManagerByUID(String uid);
}
