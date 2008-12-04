package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureSpaceStatisticsChangedEventListener extends AbstractClosureEventListener implements SpaceStatisticsChangedEventListener {

    public ClosureSpaceStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void spaceStatisticsChanged(SpaceStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}