package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface ManagingGridServiceManagerChangedEventListener extends AdminEventListener {

    void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event);
}
