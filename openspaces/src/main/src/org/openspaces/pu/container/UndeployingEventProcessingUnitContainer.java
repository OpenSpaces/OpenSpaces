package org.openspaces.pu.container;

/**
 * @author kimchy
 */
public interface UndeployingEventProcessingUnitContainer extends ProcessingUnitContainer {

    void processingUnitUndeploying();
}
