package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface ProcessingUnitStatusChangedEventListener extends AdminEventListener {

    void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event);
}