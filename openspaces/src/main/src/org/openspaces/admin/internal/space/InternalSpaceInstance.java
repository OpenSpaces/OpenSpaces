package org.openspaces.admin.internal.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.admin.SpaceConfig;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.internal.support.InternalGridComponent;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalSpaceInstance extends SpaceInstance, InternalGridComponent {

    ServiceID getServiceID();

    int getNumberOfInstances();

    int getNumberOfBackups();

    String getSpaceName();

    void setSpace(Space space);

    void setPartition(SpacePartition spacePartition);

    RuntimeHolder getRuntimeHolder() throws RemoteException;

    IJSpace getIJSpace();

    IInternalRemoteJSpaceAdmin getSpaceAdmin();

    void setMode(SpaceMode spaceMode);

    void setReplicationTargets(ReplicationTarget[] replicationTargets);
}
