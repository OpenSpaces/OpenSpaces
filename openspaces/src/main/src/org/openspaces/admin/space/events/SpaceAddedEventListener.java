package org.openspaces.admin.space.events;

import org.openspaces.admin.space.Space;

/**
 * @author kimchy
 */
public interface SpaceAddedEventListener {

    void spaceAdded(Space space);
}