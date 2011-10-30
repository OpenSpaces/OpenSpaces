package org.openspaces.admin.internal.space;

import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.internal.support.InternalGridComponent;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.internal.version.PlatformLogicalVersion;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.filters.StatisticsHolder;

/**
 * @author kimchy
 */
public interface InternalSpaceInstance extends SpaceInstance, InternalGridComponent {

    ServiceID getServiceID();

    String getClusterSchema();

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

    StatisticsHolder getStatisticsHolder() throws RemoteException;

    PlatformLogicalVersion getPlatformLogicalVersion();
}
