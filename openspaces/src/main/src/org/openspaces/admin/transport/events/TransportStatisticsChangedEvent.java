package org.openspaces.admin.transport.events;

import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportStatistics;

/**
 * @author kimchy
 */
public class TransportStatisticsChangedEvent {

    private final Transport transport;

    private final TransportStatistics statistics;

    public TransportStatisticsChangedEvent(Transport transport, TransportStatistics statistics) {
        this.transport = transport;
        this.statistics = statistics;
    }

    public Transport getTransport() {
        return transport;
    }

    public TransportStatistics getStatistics() {
        return statistics;
    }
}