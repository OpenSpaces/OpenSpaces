package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.Spaces;

/**
 * @author kimchy
 */
public interface InternalSpaces extends Spaces, InternalSpaceInstancesAware {

    void addSpace(Space space);

    InternalSpace removeSpace(String uid);

    void addSpaceInstance(SpaceInstance spaceInstance);

    SpaceInstance removeSpaceInstance(String uid);
}
