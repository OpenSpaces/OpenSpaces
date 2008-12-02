package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ManagingGridServiceManagerChangedEventListener {

    void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event);
}
