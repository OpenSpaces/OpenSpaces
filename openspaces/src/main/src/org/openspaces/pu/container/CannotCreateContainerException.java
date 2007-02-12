package org.openspaces.pu.container;

import org.openspaces.core.GigaSpaceException;

/**
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
