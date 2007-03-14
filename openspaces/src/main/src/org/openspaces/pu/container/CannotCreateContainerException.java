package org.openspaces.pu.container;

import org.openspaces.core.GigaSpaceException;

/**
 * Exception indicating failure to create a container.
 * 
 * @author kimchy
 */
public class CannotCreateContainerException extends GigaSpaceException {

    private static final long serialVersionUID = -6816021622144123429L;

    public CannotCreateContainerException(String message) {
        super(message);
    }

    public CannotCreateContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
