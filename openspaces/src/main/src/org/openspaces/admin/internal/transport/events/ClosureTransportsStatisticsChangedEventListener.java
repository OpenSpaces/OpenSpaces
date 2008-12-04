package org.openspaces.admin.internal.transport.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureTransportsStatisticsChangedEventListener extends AbstractClosureEventListener implements TransportsStatisticsChangedEventListener {

    public ClosureTransportsStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void transportsStatisticsChanged(TransportsStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}