package org.openspaces.admin.pu.events;

import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author kimchy
 */
public interface ProcessingUnitAddedEventListener {

    void processingUnitAdded(ProcessingUnit processingUnit);
}