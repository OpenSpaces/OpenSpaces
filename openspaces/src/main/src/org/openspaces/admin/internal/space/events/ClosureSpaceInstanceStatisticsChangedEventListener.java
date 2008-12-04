package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureSpaceInstanceStatisticsChangedEventListener extends AbstractClosureEventListener implements SpaceInstanceStatisticsChangedEventListener {

    public ClosureSpaceInstanceStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}