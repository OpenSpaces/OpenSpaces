package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureInstanceSpaceRemovedEventListener extends AbstractClosureEventListener implements SpaceInstanceRemovedEventListener {

    public ClosureInstanceSpaceRemovedEventListener(Object closure) {
        super(closure);
    }

    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        getClosure().call(spaceInstance);
    }
}