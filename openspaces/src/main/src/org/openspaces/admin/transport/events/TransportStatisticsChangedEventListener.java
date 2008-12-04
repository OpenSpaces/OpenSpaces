package org.openspaces.admin.transport.events;

/**
 * @author kimchy
 */
public interface TransportStatisticsChangedEventListener {

    void transportStatisticsChanged(TransportStatisticsChangedEvent event);
}