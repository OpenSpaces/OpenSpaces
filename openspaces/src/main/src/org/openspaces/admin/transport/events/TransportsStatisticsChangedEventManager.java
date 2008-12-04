package org.openspaces.admin.transport.events;

/**
 * @author kimchy
 */
public interface TransportsStatisticsChangedEventManager {

    void add(TransportsStatisticsChangedEventListener eventListener);

    void remove(TransportsStatisticsChangedEventListener eventListener);
}