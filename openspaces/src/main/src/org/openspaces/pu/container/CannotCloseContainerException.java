package org.openspaces.pu.container;

import org.openspaces.core.GigaSpaceException;

/**
 * @author kimchy
 */
public class CannotCloseContainerException extends GigaSpaceException {

    public CannotCloseContainerException(String message) {
        super(message);
    }

    public CannotCloseContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
