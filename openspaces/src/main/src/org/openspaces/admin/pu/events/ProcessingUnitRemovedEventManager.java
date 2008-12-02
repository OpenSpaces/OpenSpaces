package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitRemovedEventManager {

    void add(ProcessingUnitRemovedEventListener eventListener);

    void remove(ProcessingUnitRemovedEventListener eventListener);

}