package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface ReplicationStatusChangedEventListener {

    void replicationStatusChanged(ReplicationStatusChangedEvent event);
}