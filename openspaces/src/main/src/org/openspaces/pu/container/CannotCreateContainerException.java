package org.openspaces.pu.container;

import org.openspaces.core.GigaSpaceException;

/**
 * Exception indicating failure to create a container.
 *
 * @author kimchy
 */
public class CannotCreateContainerException extends GigaSpaceException {

    public CannotCreateContainerException(String message) {
        super(message);
    }

    public CannotCreateContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
