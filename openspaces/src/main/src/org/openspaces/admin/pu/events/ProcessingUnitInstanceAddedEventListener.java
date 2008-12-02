package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstanceAddedEventListener extends AdminEventListener {

    void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance);
}