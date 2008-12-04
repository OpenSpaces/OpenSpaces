package org.openspaces.admin.transport.events;

import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.transport.TransportsStatistics;

/**
 * @author kimchy
 */
public class TransportsStatisticsChangedEvent {

    private final Transports transports;

    private final TransportsStatistics statistics;

    public TransportsStatisticsChangedEvent(Transports transports, TransportsStatistics statistics) {
        this.transports = transports;
        this.statistics = statistics;
    }

    public Transports getTransports() {
        return transports;
    }

    public TransportsStatistics getStatistics() {
        return statistics;
    }
}