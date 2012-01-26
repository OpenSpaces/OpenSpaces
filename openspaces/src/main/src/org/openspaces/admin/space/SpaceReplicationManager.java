package org.openspaces.admin.space;

import org.openspaces.core.gateway.GatewayTarget;

/**
 * A replication manager for a single {@link Space}.
 * @author eitany
 * @since 9.0
 */
public interface SpaceReplicationManager {

    /**
     * Adds a new gateway to the {@link Space} this manager manages, will wait until the gateway is added in all
     * the {@link SpaceInstance} that belongs to this space 
     */
    void addGatewayTarget(GatewayTarget gatewayTarget); 
}
