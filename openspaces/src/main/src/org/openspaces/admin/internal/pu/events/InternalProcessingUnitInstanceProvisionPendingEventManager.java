package org.openspaces.admin.internal.pu.events;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionPendingEventManager;

public interface InternalProcessingUnitInstanceProvisionPendingEventManager extends ProcessingUnitInstanceProvisionPendingEventManager {
    
    void raisePendingEvent(ProvisionLifeCycleEvent provisionEvent, String processingUnitInstanceName);
}
