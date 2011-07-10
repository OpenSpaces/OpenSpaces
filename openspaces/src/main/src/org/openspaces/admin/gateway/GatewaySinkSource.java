package org.openspaces.admin.gateway;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public interface GatewaySinkSource {
    
    GatewaySink getSink();
    
    String getSourceGatewayName();
    
    /**
     * Bootstrap the local {@link Space} which is associated to this sink from a remote space.
     * This bootstrap request will use default timeout set by {@link Admin#setDefaultTimeout(long, TimeUnit)}
     * A bootstrap request can only be executed if this sink {@link GatewaySink#requiresBootstrapOnStartup()}. 
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait();
    /**
     * Bootstrap the local {@link Space} which is associated to this sink from a remote space.
     * This bootstrap request will use the provided timeout.
     * A bootstrap request can only be executed if this sink {@link GatewaySink#requiresBootstrapOnStartup()}.
     * @param bootstrapSourceGatewayName the name of the remote gateway to bootstrap from.
     * @return A bootstrap result
     */
    BootstrapResult bootstrapFromGatewayAndWait(long timeout, TimeUnit timeUnit);
    
}
