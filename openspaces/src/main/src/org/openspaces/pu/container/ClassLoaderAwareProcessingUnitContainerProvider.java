package org.openspaces.pu.container;

/**
 * @author kimchy
 */
public interface ClassLoaderAwareProcessingUnitContainerProvider extends ProcessingUnitContainerProvider {

    void setClassLoader(ClassLoader classLoader);
}
