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
