package org.openspaces.admin.gateway;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;

/**
 * A sink is a {@link Gateway} component which handles incoming replication from other gateways.
 * The sink is also used to bootstrap a space from another space, and it takes part in both sides of the
 * bootstrap process. It is used for initiating a bootstrap process and replicate incoming data of the
 * bootstrap process to the local {@link Space}. 
 * And it is used to respond to a remote bootstrap request by providing the relevant bootstrap data
 * to the remote sink.
 *   
 * @author eitany
 * @since 8.0.3
 */
public interface Sink {
    
    
    /**
     * Returns the gateway this sink is part of.
     */
    Gateway getGateway();
    
    /**
     * Bootstrap the local {@link Space} which is associated to this sink from a remote space.
     * This bootstrap request will use default timeout set by {@link Admin#setDefaultTimeout(long, TimeUnit)}
     * A bootstrap request can only be executed if this sink {@link Sink#requiresBootstrapOnStartup()}. 
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait(String bootstrapSourceGatewayName);
    /**
     * Bootstrap the local {@link Space} which is associated to this sink from a remote space.
     * This bootstrap request will use the provided timeout.
     * A bootstrap request can only be executed if this sink {@link Sink#requiresBootstrapOnStartup()}.
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait(String bootstrapSourceGatewayName, long timeout, TimeUnit timeUnit);
    
    /**
     * Enables incoming replication for this sink, only relevant if this sink {@link Sink#requiresBootstrapOnStartup()}
     * and no bootstrap was executed yet, otherwise the sink incoming replication is already enabled. 
     */
    void enableIncomingReplication();
    
    /**
     * Returns the source gateway names of this sink. 
     */
    String[] getSourceGatewayNames();
    
    /**
     * Returns whether this sink is configured to require a bootstrap on startup.
     */
    boolean requiresBootstrapOnStartup();
    
    /**
     * Returns the url of the space this sink is replicating in to. 
     */
    String getLocalSpaceUrl();
}
