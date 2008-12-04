package org.openspaces.admin.transport.events;

/**
 * @author kimchy
 */
public interface TransportStatisticsChangedEventManager {

    void add(TransportStatisticsChangedEventListener eventListener);

    void remove(TransportStatisticsChangedEventListener eventListener);
}