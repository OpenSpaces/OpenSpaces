package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultBackupGridServiceManagerChangedEventManager implements InternalBackupGridServiceManagerChangedEventManager {

    private final InternalAdmin admin;

    private final List<BackupGridServiceManagerChangedEventListener> listeners = new CopyOnWriteArrayList<BackupGridServiceManagerChangedEventListener>();

    public DefaultBackupGridServiceManagerChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void processingUnitBackupGridServiceManagerChanged(final BackupGridServiceManagerChangedEvent event) {
        for (final BackupGridServiceManagerChangedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitBackupGridServiceManagerChanged(event);
                }
            });
        }
    }

    public void add(BackupGridServiceManagerChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(BackupGridServiceManagerChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureBackupGridServiceManagerChangedEventListener(eventListener));
        } else {
            add((BackupGridServiceManagerChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureBackupGridServiceManagerChangedEventListener(eventListener));
        } else {
            remove((BackupGridServiceManagerChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}