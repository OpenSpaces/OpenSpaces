package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.events.SpaceAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureSpaceAddedEventListener extends AbstractClosureEventListener implements SpaceAddedEventListener {

    public ClosureSpaceAddedEventListener(Object closure) {
        super(closure);
    }

    public void spaceAdded(Space space) {
        getClosure().call(space);
    }
}