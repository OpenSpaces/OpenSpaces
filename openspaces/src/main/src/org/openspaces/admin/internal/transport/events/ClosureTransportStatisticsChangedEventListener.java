package org.openspaces.admin.internal.transport.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureTransportStatisticsChangedEventListener extends AbstractClosureEventListener implements TransportStatisticsChangedEventListener {

    public ClosureTransportStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void transportStatisticsChanged(TransportStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}