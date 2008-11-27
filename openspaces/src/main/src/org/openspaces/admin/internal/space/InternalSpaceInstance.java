package org.openspaces.admin.internal.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.internal.support.InternalGridComponent;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface InternalSpaceInstance extends SpaceInstance, InternalGridComponent {

    ServiceID getServiceID();

    int getNumberOfInstances();

    int getNumberOfBackups();

    String getSpaceName();

    void setSpace(Space space);

    SpaceConfig getSpaceConfig();

    IJSpace getIJSpace();

    IRemoteJSpaceAdmin getSpaceAdmin();
}
