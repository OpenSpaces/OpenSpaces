package org.openspaces.admin.internal.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.client.SpaceURL;
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

    private final SpaceURL spaceURL;

    private final int instanceId;

    private final int backupId;

    private volatile Space space;

    public DefaultSpaceInstance(ServiceID serviceID, IJSpace ijSpace, IInternalRemoteJSpaceAdmin spaceAdmin,
                                SpaceConfig spaceConfig) {
        this.uid = serviceID.toString();
        this.serviceID = serviceID;
        this.ijSpace = ijSpace;
        this.spaceAdmin = spaceAdmin;
        this.spaceConfig = spaceConfig;
        this.spaceURL = ijSpace.getURL();
        String sInstanceId = spaceURL.getProperty(SpaceURL.CLUSTER_MEMBER_ID);
        if (sInstanceId == null || sInstanceId.length() == 0) {
            instanceId = 1;
        } else {
            instanceId = Integer.parseInt(sInstanceId);
        }
        String sBackupId = spaceURL.getProperty(SpaceURL.CLUSTER_BACKUP_ID);
        if (sBackupId == null || sBackupId.length() == 0) {
            backupId = 0;
        } else {
            backupId = Integer.parseInt(sBackupId);
        }
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
        return instanceId;
    }

    public int getBackupId() {
        return backupId;
    }

    public SpaceConfig getSpaceConfig() {
        return this.spaceConfig;
    }

    public IJSpace getIJSpace() {
        return this.ijSpace;
    }

    public IRemoteJSpaceAdmin getSpaceAdmin() {
        return this.spaceAdmin;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }
}
