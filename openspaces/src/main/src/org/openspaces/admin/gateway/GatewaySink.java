package org.openspaces.admin.gateway;

import org.openspaces.admin.space.Space;

/**
 * A sink is a {@link GatewayProcessingUnit} component which handles incoming replication from other gateways.
 * The sink is also used to bootstrap a space from another space, and it takes part in both sides of the
 * bootstrap process. It is used for initiating a bootstrap process and replicate incoming data of the
 * bootstrap process to the local {@link Space}. 
 * And it is used to respond to a remote bootstrap request by providing the relevant bootstrap data
 * to the remote sink.
 *   
 * @author eitany
 * @since 8.0.4
 */
public interface GatewaySink {
    
    
    /**
     * Returns the gateway this sink is part of.
     */
    GatewayProcessingUnit getGatewayProcessingUnit();
    
    /**
     * Enables incoming replication for this sink, only relevant if this sink {@link GatewaySink#requiresBootstrapOnStartup()}
     * and no bootstrap was executed yet, otherwise the sink incoming replication is already enabled. 
     */
    void enableIncomingReplication();
    
    /**
     * Returns the gateway sink sources of this sink. 
     */
    GatewaySinkSource[] getSources();
    
    /**
     * Returns <code>true</code> if this sink has a source gateway with the specified name; <code>false</code> otherwise.
     */
    boolean containsSource(String sourceGatewayName);
    
    /**
     * Returns a gateway sink source for the specified source gateway name or null if no such gateway sink source exists. 
     */
    GatewaySinkSource getSourceByName(String sourceGatewayName);
    
    /**
     * Returns whether this sink is configured to require a bootstrap on startup.
     */
    boolean requiresBootstrapOnStartup();
    
    /**
     * Returns the url of the space this sink is replicating in to. 
     */
    String getLocalSpaceUrl();
}
