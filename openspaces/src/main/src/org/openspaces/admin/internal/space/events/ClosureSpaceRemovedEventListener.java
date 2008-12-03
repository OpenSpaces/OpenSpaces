package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.events.SpaceRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureSpaceRemovedEventListener extends AbstractClosureEventListener implements SpaceRemovedEventListener {

    public ClosureSpaceRemovedEventListener(Object closure) {
        super(closure);
    }

    public void spaceRemoved(Space space) {
        getClosure().call(space);
    }
}