package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstanceAddedEventManager {

    void add(ProcessingUnitInstanceAddedEventListener eventListener);

    void remove(ProcessingUnitInstanceAddedEventListener eventListener);

}