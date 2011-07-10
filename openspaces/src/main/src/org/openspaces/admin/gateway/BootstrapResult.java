package org.openspaces.admin.gateway;

/**
 * A result of a bootstrap
 * @see GatewaySink#bootstrapFromGatewayAndWait(String, long, java.util.concurrent.TimeUnit)
 * 
 * @author eitany
 * @since 8.0.4
 */
public interface BootstrapResult {
   
    /**
     * Returns <code>true</code> if the bootstrap succeeded, <code>false</code> otherwise. 
     */
    boolean isSucceeded();
    
    /**
     * Returns the failure cause for the bootstrap or <code>null</code> if the bootstrap succeeded. 
     */
    Throwable getFailureCause();

}
