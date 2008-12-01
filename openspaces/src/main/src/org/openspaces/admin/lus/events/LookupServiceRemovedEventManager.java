package org.openspaces.admin.lus.events;

/**
 * @author kimchy
 */
public interface LookupServiceRemovedEventManager {

    void add(LookupServiceRemovedEventListener eventListener);

    void remove(LookupServiceRemovedEventListener eventListener);

}