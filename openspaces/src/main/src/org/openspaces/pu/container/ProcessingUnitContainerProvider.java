package org.openspaces.pu.container;

/**
 * @author kimchy
 */
public interface ProcessingUnitContainerProvider {

    ProcessingUnitContainer createContainer() throws CannotCreateContainerException;
}
