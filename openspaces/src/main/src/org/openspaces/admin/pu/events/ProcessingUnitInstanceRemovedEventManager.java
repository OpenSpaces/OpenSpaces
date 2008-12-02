package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstanceRemovedEventManager {

    void add(ProcessingUnitInstanceRemovedEventListener eventListener);

    void remove(ProcessingUnitInstanceRemovedEventListener eventListener);

}