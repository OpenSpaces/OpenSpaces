package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface ProcessingUnitSpaceCorrelatedEventListener extends AdminEventListener {

    void processingUnitSpaceCorrelated(ProcessingUnitSpaceCorrelatedEvent event);
}