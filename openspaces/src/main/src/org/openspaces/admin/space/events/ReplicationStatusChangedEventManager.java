package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface ReplicationStatusChangedEventManager {

    void add(ReplicationStatusChangedEventListener eventListener);

    void remove(ReplicationStatusChangedEventListener eventListener);
}