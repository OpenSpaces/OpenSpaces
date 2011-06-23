package org.openspaces.admin.gateway;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;

/**
 * A sink is a {@link Gateway} component which handles incoming replication.
 * The sink is used to bootstrap a space from another space, and it takes part in both sides of the
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
     * Bootstrap the local {@link Space} which is associated to this sink from a remote space.
     * This bootstrap request will use default timeout set by {@link Admin#setDefaultTimeout(long, TimeUnit)}
     * A bootstrap request can only be executed if this sink was started with "requires-bootstrap". 
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait(String bootstrapSourceGatewayName);
    /**
     * Bootstrap the local {@link Space} which is associated to this sink from a remote space.
     * This bootstrap request will use the provided timeout.
     * A bootstrap request can only be executed if this sink is started with "requires-bootstrap".
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait(String bootstrapSourceGatewayName, long timeout, TimeUnit timeUnit);
    
    /**
     * Enables incoming replication for this sink, only relevant if this sink was started with "requires-bootstrap".
     * and no bootstrap was executed yet. 
     */
    void enableIncomingReplication();
}
