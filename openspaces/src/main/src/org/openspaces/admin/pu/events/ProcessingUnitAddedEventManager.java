package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitAddedEventManager {

    void add(ProcessingUnitAddedEventListener eventListener);

    void remove(ProcessingUnitAddedEventListener eventListener);

}