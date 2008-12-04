package org.openspaces.admin.transport;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventManager;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventManager;

/**
 * @author kimchy
 */
public interface Transports extends Iterable<Transport>, AdminAware, StatisticsMonitor {

    Transport[] getTransports();

    Transport[] getTransports(String host);

    Transport getTransportByHostAndPort(String host, int port);

    Transport getTransportByUID(String uid);

    int size();

    TransportsStatistics getStatistics();

    TransportStatisticsChangedEventManager getTransportStatisticsChanged();

    TransportsStatisticsChangedEventManager getStatisticsChanged();
}
