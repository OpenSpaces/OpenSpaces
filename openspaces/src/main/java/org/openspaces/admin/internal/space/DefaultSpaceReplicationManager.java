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

import java.util.concurrent.ExecutionException;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.space.SpaceReplicationManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.gateway.GatewayTarget;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.cluster.node.impl.gateway.GatewayPolicy;
import com.gigaspaces.internal.cluster.node.impl.gateway.GatewaysPolicy;
import com.j_spaces.core.cluster.ClusterPolicy;
import com.j_spaces.core.cluster.ReplicationPolicy;

/**
 * 
 * @author eitany
 * @since 9.0
 */
public class DefaultSpaceReplicationManager implements SpaceReplicationManager {

    private final DefaultSpace _defaultSpace;

    public DefaultSpaceReplicationManager(DefaultSpace defaultSpace) {
        _defaultSpace = defaultSpace;
    }

    @Override
    public void addGatewayTarget(GatewayTarget gatewayTarget) {
        GigaSpace gigaSpace = _defaultSpace.getGigaSpace();
        ISpaceProxy spaceProxy = (ISpaceProxy) gigaSpace.getSpace();
        ClusterPolicy clusterPolicy = spaceProxy.getDirectProxy().getClusterPolicy();
        if (clusterPolicy == null)
            throw new UnsupportedOperationException("Cannot add replication gateway target to a non clustered space");
        ReplicationPolicy replicationPolicy = clusterPolicy.getReplicationPolicy();
        if (replicationPolicy == null)
            throw new UnsupportedOperationException("Cannot add replication gateway target to a non replicated space");
        GatewaysPolicy gatewaysPolicy = replicationPolicy.getGatewaysPolicy();
        if (gatewaysPolicy == null)
            throw new UnsupportedOperationException("Cannot add replication gateway target to a space with no gateways");
        GatewayPolicy defaultGatewayPolicy = gatewaysPolicy.getDefaultGatewayPolicy();        
        try {            
            GatewayPolicy gatewayPolicy = defaultGatewayPolicy != null? gatewayTarget.asGatewayPolicy(defaultGatewayPolicy) : gatewayTarget.asGatewayPolicy();
            spaceProxy.addReplicationGatewayTarget(gatewayPolicy);
        } catch (ExecutionException e) {
            throw new AdminException("failed to add a replication gateway target", e.getCause()); 
        } catch (Exception e) {
            throw new AdminException("failed to add a replication gateway target", e);
        }
    }
    
    @Override
    public void removeGatewayTarget(String gatewayTargetName) {
        GigaSpace gigaSpace = _defaultSpace.getGigaSpace();
        ISpaceProxy spaceProxy = (ISpaceProxy) gigaSpace.getSpace();
        try {
            spaceProxy.removeGatewayTarget(gatewayTargetName);
        } catch (ExecutionException e) {
            throw new AdminException("failed to remove a replication gateway target", e.getCause()); 
        } catch (Exception e) {
            throw new AdminException("failed to remove a replication gateway target", e);
        }
    }

}
