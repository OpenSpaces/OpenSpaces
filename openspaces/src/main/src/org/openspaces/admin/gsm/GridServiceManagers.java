package org.openspaces.admin.gsm;

import java.util.Map;

/**
 * @author kimchy
 */
public interface GridServiceManagers extends Iterable<GridServiceManager> {

    GridServiceManager[] getManagers();

    GridServiceManager getManagerByUID(String uid);

    Map<String, GridServiceManager> getUids();

    int getSize();

    boolean isEmpty();

    void addEventListener(GridServiceManagerEventListener eventListener);

    void removeEventListener(GridServiceManagerEventListener eventListener);
}
