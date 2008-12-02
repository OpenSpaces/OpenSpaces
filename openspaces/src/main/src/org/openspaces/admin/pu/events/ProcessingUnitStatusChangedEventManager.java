package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitStatusChangedEventManager {

    void add(ProcessingUnitStatusChangedEventListener eventListener);

    void remove(ProcessingUnitStatusChangedEventListener eventListener);
}