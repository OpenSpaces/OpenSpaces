package org.openspaces.admin.gsm.events;

import org.openspaces.admin.gsm.GridServiceManager;

/**
 * @author kimchy
 */
public interface GridServiceManagerRemovedEventListener {

    void gridServiceManagerRemoved(GridServiceManager gridServiceManager);
}