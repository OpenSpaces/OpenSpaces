package org.openspaces.admin.internal.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.space.Space;

/**
 * @author kimchy
 */
public class DefaultSpaceInstance implements InternalSpaceInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final IJSpace ijSpace;

    private final IInternalRemoteJSpaceAdmin spaceAdmin;

    private final SpaceConfig spaceConfig;

    private volatile Space space;

    public DefaultSpaceInstance(ServiceID serviceID, IJSpace ijSpace, IInternalRemoteJSpaceAdmin spaceAdmin, SpaceConfig spaceConfig) {
        this.uid = serviceID.toString();
        this.serviceID = serviceID;
        this.ijSpace = ijSpace;
        this.spaceAdmin = spaceAdmin;
        this.spaceConfig = spaceConfig;
    }

    public String getUID() {
        return uid;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    public String getSpaceName() {
        return spaceConfig.getSpaceName();
    }

    public int getInstanceId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getBackupId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }
}
