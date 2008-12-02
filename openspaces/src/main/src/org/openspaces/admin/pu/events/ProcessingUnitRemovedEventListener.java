package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author kimchy
 */
public interface ProcessingUnitRemovedEventListener extends AdminEventListener {

    void processingUnitRemoved(ProcessingUnit processingUnit);
}