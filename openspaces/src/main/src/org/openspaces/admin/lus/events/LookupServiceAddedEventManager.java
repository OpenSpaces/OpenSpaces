package org.openspaces.admin.lus.events;

/**
 * @author kimchy
 */
public interface LookupServiceAddedEventManager {

    void add(LookupServiceAddedEventListener eventListener);

    void remove(LookupServiceAddedEventListener eventListener);

}