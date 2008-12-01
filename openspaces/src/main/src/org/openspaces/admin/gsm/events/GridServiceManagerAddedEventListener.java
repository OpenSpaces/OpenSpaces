package org.openspaces.admin.gsm.events;

import org.openspaces.admin.gsm.GridServiceManager;

/**
 * @author kimchy
 */
public interface GridServiceManagerAddedEventListener {

    void gridServiceManagerAdded(GridServiceManager gridServiceManager);
}