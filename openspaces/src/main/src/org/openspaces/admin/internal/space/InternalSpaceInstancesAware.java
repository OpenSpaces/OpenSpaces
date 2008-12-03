package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface InternalSpaceInstancesAware {

    SpaceInstance[] getSpaceInstances();
}
