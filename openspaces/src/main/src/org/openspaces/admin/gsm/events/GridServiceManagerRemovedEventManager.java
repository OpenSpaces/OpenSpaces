package org.openspaces.admin.gsm.events;

/**
 * @author kimchy
 */
public interface GridServiceManagerRemovedEventManager {

    void add(GridServiceManagerRemovedEventListener eventListener);

    void remove(GridServiceManagerRemovedEventListener eventListener);

}