package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface BackupGridServiceManagerChangedEventListener {

    void processingUnitBackupGridServiceManagerChanged(BackupGridServiceManagerChangedEvent event);
}