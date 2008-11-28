package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;

/**
 * @author kimchy
 */
public interface InternalSpacePartition extends SpacePartition {

    void addSpaceInstance(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);
}
