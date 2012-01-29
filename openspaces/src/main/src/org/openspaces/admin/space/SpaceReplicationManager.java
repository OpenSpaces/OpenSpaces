package org.openspaces.admin.space;

import org.openspaces.core.gateway.GatewayTarget;

/**
 * A replication manager for a single {@link Space}.
 * @author eitany
 * @since 9.0
 */
public interface SpaceReplicationManager {

    /**
     * Adds a new gateway target to the {@link Space} this manager manages, will wait until the gateway target is added in all
     * of the {@link SpaceInstance}s that belong to this space. 
     */
    void addGatewayTarget(GatewayTarget gatewayTarget); 
    
    /**
     * Removes an existing gateway target from the {@link Space} this manager manages, will wait until the gateway target is removed from all
     * of the {@link SpaceInstance}s that belong to this space. 
     */
    void removeGatewayTarget(String gatewayTargetName);
}
