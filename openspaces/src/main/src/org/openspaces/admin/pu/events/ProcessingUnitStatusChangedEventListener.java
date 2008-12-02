package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitStatusChangedEventListener {

    void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event);
}