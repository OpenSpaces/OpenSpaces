package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface ProcessingUnitInstanceRemovedEventListener extends AdminEventListener {

    void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance);
}