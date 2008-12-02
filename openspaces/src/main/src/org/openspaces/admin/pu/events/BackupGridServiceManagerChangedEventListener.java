package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface BackupGridServiceManagerChangedEventListener extends AdminEventListener {

    void processingUnitBackupGridServiceManagerChanged(BackupGridServiceManagerChangedEvent event);
}