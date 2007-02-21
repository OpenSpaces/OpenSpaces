package org.openspaces.pu.container;

/**
 * A processing unit container provider is responsible for creating
 * {@link org.openspaces.pu.container.ProcessingUnitContainer}. Usually concrete implementation
 * will have additional parameters controlling the nature of how specific container will be
 * created.
 *
 * @author kimchy
 */
public interface ProcessingUnitContainerProvider {

    /**
     * Creates a processing unit container.
     *
     * @return A newly created processing unit container.
     * @throws CannotCreateContainerException
     */
    ProcessingUnitContainer createContainer() throws CannotCreateContainerException;
}
