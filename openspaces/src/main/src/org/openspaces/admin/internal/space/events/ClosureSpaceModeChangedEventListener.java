package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureSpaceModeChangedEventListener extends AbstractClosureEventListener implements SpaceModeChangedEventListener {

    public ClosureSpaceModeChangedEventListener(Object closure) {
        super(closure);
    }

    public void spaceModeChanged(SpaceModeChangedEvent event) {
        getClosure().call(event);
    }
}