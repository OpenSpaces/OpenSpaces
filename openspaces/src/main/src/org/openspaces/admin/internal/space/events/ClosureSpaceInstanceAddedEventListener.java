package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureSpaceInstanceAddedEventListener extends AbstractClosureEventListener implements SpaceInstanceAddedEventListener {

    public ClosureSpaceInstanceAddedEventListener(Object closure) {
        super(closure);
    }

    public void spaceInstanceAdded(SpaceInstance spaceInstance) {
        getClosure().call(spaceInstance);
    }
}