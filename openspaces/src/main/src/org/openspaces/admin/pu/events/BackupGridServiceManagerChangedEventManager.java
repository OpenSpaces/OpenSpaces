package org.openspaces.admin.pu.events;

/**
 * @author kimchy
 */
public interface BackupGridServiceManagerChangedEventManager {

    void add(BackupGridServiceManagerChangedEventListener eventListener);

    void remove(BackupGridServiceManagerChangedEventListener eventListener);
}