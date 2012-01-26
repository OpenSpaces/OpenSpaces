package org.openspaces.admin.space;

import org.openspaces.core.gateway.GatewayTarget;

/**
 * A replication manager for a single {@link Space}.
 * @author eitany
 * @since 9.0
 */
public interface SpaceReplicationManager {

    /**
     * Adds a new gateway target to the {@link Space} this manager manages, will wait until the gateway is added in all
     * the {@link SpaceInstance} that belongs to this space 
     */
    void addGatewayTarget(GatewayTarget gatewayTarget); 
    
    /**
     * Removes an existing gateway target from the {@link Space} this manager manages, will wait until the gateway is removed from all
     * the {@link SpaceInstance} that belongs to this space 
     */
    void removeGatewayTarget(String gatewayTargetName);
}
