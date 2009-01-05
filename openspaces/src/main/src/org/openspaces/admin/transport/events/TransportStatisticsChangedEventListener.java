package org.openspaces.admin.transport.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface TransportStatisticsChangedEventListener extends AdminEventListener {

    void transportStatisticsChanged(TransportStatisticsChangedEvent event);
}