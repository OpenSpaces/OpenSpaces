package org.openspaces.admin.internal.space;

import org.openspaces.admin.Admin;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface InternalSpace extends Space, InternalSpaceInstancesAware {

    void addInstance(SpaceInstance spaceInstance);

    InternalSpaceInstance removeInstance(String uid);

    void refreshScheduledSpaceMonitors();

    Admin getAdmin();
}
