package org.openspaces.admin.internal.os.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureOperatingSystemsStatisticsChangedEventListener extends AbstractClosureEventListener implements OperatingSystemsStatisticsChangedEventListener {

    public ClosureOperatingSystemsStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void operatingSystemsStatisticsChanged(OperatingSystemsStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}