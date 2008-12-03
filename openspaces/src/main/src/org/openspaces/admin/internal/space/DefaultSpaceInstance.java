package org.openspaces.admin.internal.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.client.SpaceURL;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.DefaultSpaceModeChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceModeChangedEventManager;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultSpaceInstance extends AbstractGridComponent implements InternalSpaceInstance {

    private final String uid;

    private final ServiceID serviceID;

    private final IJSpace ijSpace;

    private final IInternalRemoteJSpaceAdmin spaceAdmin;

    private final SpaceConfig spaceConfig;

    private final SpaceURL spaceURL;

    private final int numberOfInstances;

    private final int numberOfBackups;

    private final int instanceId;

    private final int backupId;

    private volatile Space space;

    private volatile SpacePartition spacePartition;

    private volatile SpaceMode spaceMode = SpaceMode.NONE;

    private volatile ReplicationTarget[] replicationTargets = new ReplicationTarget[0];

    private final InternalSpaceModeChangedEventManager spaceModeChangedEventManager;

    public DefaultSpaceInstance(ServiceID serviceID, IJSpace ijSpace, IInternalRemoteJSpaceAdmin spaceAdmin,
                                SpaceConfig spaceConfig, InternalAdmin admin) {
        super(admin);
        this.uid = serviceID.toString();
        this.serviceID = serviceID;
        this.ijSpace = ijSpace;
        this.spaceAdmin = spaceAdmin;
        this.spaceConfig = spaceConfig;
        this.spaceURL = ijSpace.getURL();

        this.spaceModeChangedEventManager = new DefaultSpaceModeChangedEventManager(admin);
        
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
        String totalMembers = spaceURL.getProperty(SpaceURL.CLUSTER_TOTAL_MEMBERS);
        if (totalMembers == null || totalMembers.length() == 0) {
            numberOfInstances = 1;
            numberOfBackups = 0;
        } else {
            int index = totalMembers.indexOf(',');
            if (index > 0) {
                numberOfInstances = Integer.parseInt(totalMembers.substring(0, index));
                numberOfBackups = Integer.parseInt(totalMembers.substring(index + 1));
            } else {
                numberOfInstances = Integer.parseInt(totalMembers);
                numberOfBackups = 0;
            }
        }
    }

    public String getUid() {
        return uid;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public int getNumberOfBackups() {
        return numberOfBackups;
    }

    public String getSpaceName() {
        return spaceConfig.getSpaceName();
    }

    public SpaceModeChangedEventManager getSpaceModeChanged() {
        return this.spaceModeChangedEventManager;
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

    public IInternalRemoteJSpaceAdmin getSpaceAdmin() {
        return this.spaceAdmin;
    }

    public void setMode(SpaceMode spaceMode) {
        SpaceMode previousSpaceMode = this.spaceMode;
        SpaceMode newSpaceMode = spaceMode;
        if (previousSpaceMode != newSpaceMode) {
            SpaceModeChangedEvent event = new SpaceModeChangedEvent(this, previousSpaceMode, newSpaceMode);
            spaceModeChangedEventManager.spaceModeChanged(event);
            ((InternalSpaceModeChangedEventManager) getSpace().getSpaceModeChanged()).spaceModeChanged(event);
            ((InternalSpaceModeChangedEventManager) getSpace().getSpaces().getSpaceModeChanged()).spaceModeChanged(event);
        }
        this.spaceMode = spaceMode;
    }

    public SpaceMode getMode() {
        return this.spaceMode;
    }

    public ReplicationTarget[] getReplicationTargets() {
        return replicationTargets;
    }

    public void setReplicationTargets(ReplicationTarget[] replicationTargets) {
        this.replicationTargets = replicationTargets;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public void setPartition(SpacePartition spacePartition) {
        this.spacePartition = spacePartition;
    }

    public SpacePartition getPartition() {
        return this.spacePartition;
    }

    public NIODetails getNIODetails() throws RemoteException {
        return spaceAdmin.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return spaceAdmin.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return spaceAdmin.getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return spaceAdmin.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return spaceAdmin.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return spaceAdmin.getJVMStatistics();
    }
}
