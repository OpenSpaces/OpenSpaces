/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.servicegrid;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.jvm.JVMInfoProvider;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.operatingsystem.OSInfoProvider;
import com.gigaspaces.grid.zone.GridZoneProvider;
import com.j_spaces.core.client.SpaceURL;
import org.openspaces.core.cluster.ClusterInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface PUServiceBean extends NIOInfoProvider, OSInfoProvider, JVMInfoProvider, GridZoneProvider, Remote {

    /**
     * Should this member be checked to see if it is alive or not.
     */
    boolean isMemberAliveEnabled() throws RemoteException;

    /**
     * Return <code>true</code> if the member is alive or not. Exception indicates that
     * the member is not alive (and allows for further information).
     */
    boolean isAlive() throws RemoteException, Exception;

    Object[] listServiceDetails() throws RemoteException;

    ClusterInfo getClusterInfo() throws RemoteException;

    PUDetails getPUDetails() throws RemoteException;

    PUMonitors getPUMonitors() throws RemoteException;

    SpaceURL[] listSpacesURLs() throws RemoteException;
    
    SpaceMode[] listSpacesModes() throws RemoteException;

    void destroy() throws RemoteException;
}
