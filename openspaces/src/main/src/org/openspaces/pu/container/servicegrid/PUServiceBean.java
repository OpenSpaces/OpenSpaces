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

import com.gigaspaces.annotation.lrmi.AsyncRemoteCall;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.grid.zone.GridZoneProvider;
import com.gigaspaces.internal.jvm.JVMInfoProvider;
import com.gigaspaces.internal.os.OSInfoProvider;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.filters.StatisticsHolder;
import com.sun.jini.start.ServiceProxyAccessor;
import net.jini.core.lookup.ServiceID;
import org.jini.rio.core.jsb.ServiceState;
import org.jini.rio.resources.servicecore.Service;
import org.openspaces.core.cluster.ClusterInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * @author kimchy
 */
public interface PUServiceBean extends NIOInfoProvider, OSInfoProvider, JVMInfoProvider, GridZoneProvider, Remote, ServiceState, ServiceProxyAccessor, Service {

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

    String getPresentationName() throws RemoteException;

    PUDetails getPUDetails() throws RemoteException;

    PUMonitors getPUMonitors() throws RemoteException;

    IJSpace getSpaceDirect(ServiceID serviceID) throws RemoteException;

    RuntimeHolder getSpaceRuntimeHolder(ServiceID serviceID) throws RemoteException;

    StatisticsHolder getSpaceStatisticsHolder(ServiceID serviceID) throws RemoteException;

    SpaceURL[] listSpacesURLs() throws RemoteException;

    SpaceMode[] listSpacesModes() throws RemoteException;

    @AsyncRemoteCall
    Object invoke(String serviceBeanName, Map<String, Object> namedArgs) throws RemoteException;
    
    boolean isStopping() throws RemoteException;
}
