package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ManagingGridServiceManagerChangedEventManager {

    void add(ManagingGridServiceManagerChangedEventListener eventListener);

    void remove(ManagingGridServiceManagerChangedEventListener eventListener);
}