package org.openspaces.admin.gsm.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsm.GridServiceManager;

/**
 * @author kimchy
 */
public interface GridServiceManagerAddedEventListener extends AdminEventListener {

    void gridServiceManagerAdded(GridServiceManager gridServiceManager);
}