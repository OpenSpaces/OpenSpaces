package org.openspaces.admin.space.events;

import org.openspaces.admin.space.Space;

/**
 * @author kimchy
 */
public interface SpaceRemovedEventListener {

    void spaceRemoved(Space space);
}