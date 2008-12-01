package org.openspaces.admin.gsm.events;

/**
 * @author kimchy
 */
public interface GridServiceManagerAddedEventManager {

    void add(GridServiceManagerAddedEventListener eventListener);

    void remove(GridServiceManagerAddedEventListener eventListener);

}