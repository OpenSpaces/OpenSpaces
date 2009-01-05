package org.openspaces.admin.transport.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface TransportsStatisticsChangedEventListener extends AdminEventListener {

    void transportsStatisticsChanged(TransportsStatisticsChangedEvent event);
}