package org.openspaces.admin.internal.pu.events;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionSuccessEventManager;

public interface InternalProcessingUnitInstanceProvisionSuccessEventManager extends ProcessingUnitInstanceProvisionSuccessEventManager {
    
    void raiseSuccessEvent(ProvisionLifeCycleEvent provisionEvent, ProcessingUnitInstance processingUnitInstance);
}
