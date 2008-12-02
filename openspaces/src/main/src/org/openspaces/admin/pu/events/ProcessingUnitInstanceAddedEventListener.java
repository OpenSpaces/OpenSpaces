package org.openspaces.admin.pu.events;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstanceAddedEventListener {

    void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance);
}