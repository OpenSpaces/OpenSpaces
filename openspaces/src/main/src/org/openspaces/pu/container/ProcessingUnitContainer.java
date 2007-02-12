package org.openspaces.pu.container;

/**
 * @author kimchy
 */
public interface ProcessingUnitContainer {

    void close() throws CannotCloseContainerException;
}
