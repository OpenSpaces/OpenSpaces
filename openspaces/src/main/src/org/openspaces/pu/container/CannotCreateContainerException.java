package org.openspaces.pu.container;

/**
 * Exception indicating failure to create a container.
 * 
 * @author kimchy
 */
public class CannotCreateContainerException extends ProcessingUnitContainerException {

    private static final long serialVersionUID = -6816021622144123429L;

    public CannotCreateContainerException(String message) {
        super(message);
    }

    public CannotCreateContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
