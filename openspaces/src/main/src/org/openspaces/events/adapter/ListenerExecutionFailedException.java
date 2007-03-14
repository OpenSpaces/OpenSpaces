package org.openspaces.events.adapter;

import org.openspaces.core.GigaSpaceException;

/**
 * Represents a listener execution failure.
 * 
 * @author kimchy
 */
public class ListenerExecutionFailedException extends GigaSpaceException {

    private static final long serialVersionUID = 7502177620008347109L;

    public ListenerExecutionFailedException(String message) {
        super(message);
    }

    public ListenerExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
