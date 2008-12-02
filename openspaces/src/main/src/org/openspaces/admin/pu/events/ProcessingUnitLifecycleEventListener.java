package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface ProcessingUnitLifecycleEventListener extends ProcessingUnitAddedEventListener, ProcessingUnitRemovedEventListener,
        ProcessingUnitStatusChangedEventListener, ManagingGridServiceManagerChangedEventListener, BackupGridServiceManagerChangedEventListener {
}
