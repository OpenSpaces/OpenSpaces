package org.openspaces.admin.internal.space;

import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface InternalSpaceInstance extends SpaceInstance {

    ServiceID getServiceID();

    String getSpaceName();

    void setSpace(Space space);
}
