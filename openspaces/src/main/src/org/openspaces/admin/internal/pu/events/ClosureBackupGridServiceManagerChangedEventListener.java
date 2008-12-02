package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureBackupGridServiceManagerChangedEventListener extends AbstractClosureEventListener implements BackupGridServiceManagerChangedEventListener {

    public ClosureBackupGridServiceManagerChangedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitBackupGridServiceManagerChanged(BackupGridServiceManagerChangedEvent event) {
        getClosure().call(event);
    }
}