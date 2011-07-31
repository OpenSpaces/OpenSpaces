package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent.Type;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultBackupGridServiceManagerChangedEventManager implements InternalBackupGridServiceManagerChangedEventManager {

    private final InternalAdmin admin;

    private final List<BackupGridServiceManagerChangedEventListener> listeners = new CopyOnWriteArrayList<BackupGridServiceManagerChangedEventListener>();

    private final ProcessingUnit processingUnit;

    public DefaultBackupGridServiceManagerChangedEventManager(InternalAdmin admin) {
        this(admin, null);
    }
    
    public DefaultBackupGridServiceManagerChangedEventManager(InternalAdmin admin, ProcessingUnit processingUnit) {
        this.admin = admin;
        this.processingUnit = processingUnit;
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
        add(eventListener, true);
    }
    
    public void add(final BackupGridServiceManagerChangedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    if (processingUnit == null) {
                        for (ProcessingUnit pu : admin.getProcessingUnits()) {
                            for (GridServiceManager backupGsm : pu.getBackupGridServiceManagers()) {
                                eventListener.processingUnitBackupGridServiceManagerChanged(new BackupGridServiceManagerChangedEvent(pu, Type.ADDED, backupGsm));
                            }
                        }
                    } else {
                        for (GridServiceManager backupGsm : processingUnit.getBackupGridServiceManagers()) {
                            eventListener.processingUnitBackupGridServiceManagerChanged(new BackupGridServiceManagerChangedEvent(processingUnit, Type.ADDED, backupGsm));
                        }
                    }
                }
            });
        }
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