package org.openspaces.pu.container;

/**
 * Exception indicating failure to close a container.
 * 
 * @author kimchy
 */
public class CannotCloseContainerException extends ProcessingUnitContainerException {

    private static final long serialVersionUID = -933652672759514319L;

    public CannotCloseContainerException(String message) {
        super(message);
    }

    public CannotCloseContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
