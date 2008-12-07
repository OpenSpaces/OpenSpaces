package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitSpaceCorrelatedEventManager {

    void add(ProcessingUnitSpaceCorrelatedEventListener eventListener);

    void remove(ProcessingUnitSpaceCorrelatedEventListener eventListener);
}