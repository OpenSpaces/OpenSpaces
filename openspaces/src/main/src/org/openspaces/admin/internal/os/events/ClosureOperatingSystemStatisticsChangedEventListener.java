package org.openspaces.admin.internal.os.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureOperatingSystemStatisticsChangedEventListener extends AbstractClosureEventListener implements OperatingSystemStatisticsChangedEventListener {

    public ClosureOperatingSystemStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}