package org.openspaces.events.adapter;

import org.springframework.dao.DataAccessException;

/**
 * Represents a listener execution failure.
 * 
 * @author kimchy
 */
public class ListenerExecutionFailedException extends DataAccessException {

    private static final long serialVersionUID = 7502177620008347109L;

    public ListenerExecutionFailedException(String message) {
        super(message);
    }

    public ListenerExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
