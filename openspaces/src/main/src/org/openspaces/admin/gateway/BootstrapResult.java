package org.openspaces.admin.gateway;

/**
 * A result of a bootstrap
 * @see Sink#bootstrapFromGatewayAndWait(String, long, java.util.concurrent.TimeUnit)
 * 
 * @author eitany
 * @since 8.0.3
 */
public interface BootstrapResult {
   
    /**
     * Returns <code>true</code> if the bootstrap succeeded, <code>false</code> otherwise. 
     */
    boolean isSucceeded();
    
    /**
     * Returns <code>true</code> if the bootstrap failed , <code>false</code> otherwise. 
     */
    boolean isFailed();
    
    /**
     * Returns the failure cause for the bootstrap or <code>null</code> if the bootstrap succeeded. 
     */
    Throwable getFailureCause();

}
