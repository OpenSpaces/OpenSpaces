package org.openspaces.admin.gsm;

/**
 * @author kimchy
 */
public interface GridServiceManagers extends Iterable<GridServiceManager> {

    GridServiceManager[] getManagers();

    GridServiceManager getManagerByUID(String uid);

    int size();

    boolean isEmpty();
}
