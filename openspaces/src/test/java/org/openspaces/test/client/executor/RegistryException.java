package org.openspaces.test.client.executor;

/**
 * An encapsulation of exceptions that might be thrown while interacting
 * with a registry.
 */
public class RegistryException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public RegistryException(String message, Throwable cause) {
        super(message + "\n Caused by: " + cause.toString(), cause);
    }
    
}