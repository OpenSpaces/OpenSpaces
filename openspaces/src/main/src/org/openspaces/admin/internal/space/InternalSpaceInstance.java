/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
