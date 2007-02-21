package org.openspaces.pu.container;

/**
 * A processing unit container represents a currently running processing unit context.
 *
 * @author kimchy
 */
public interface ProcessingUnitContainer {

    /**
     * Closes the given processing unit container.
     *
     * @throws CannotCloseContainerException
     */
    void close() throws CannotCloseContainerException;
}
