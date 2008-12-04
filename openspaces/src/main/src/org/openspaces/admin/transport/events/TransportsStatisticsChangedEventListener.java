package org.openspaces.admin.transport.events;

/**
 * @author kimchy
 */
public interface TransportsStatisticsChangedEventListener {

    void transportsStatisticsChanged(TransportsStatisticsChangedEvent event);
}