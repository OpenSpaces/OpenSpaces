package org.openspaces.admin.gsm;

/**
 * @author kimchy
 */
public interface GridServiceManagerEventListener {

    void gridServiceManagerAdded(GridServiceManager gridServiceManager);

    void gridServiceManagerRemoved(GridServiceManager gridServiceManager);
}
