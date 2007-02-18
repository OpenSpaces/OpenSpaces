package org.openspaces.event.adapter;

import org.openspaces.core.GigaSpaceException;

/**
 * @author kimchy
 */
public class ListenerExecutionFailedException extends GigaSpaceException {

    public ListenerExecutionFailedException(String message) {
        super(message);
    }

    public ListenerExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
