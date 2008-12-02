package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author kimchy
 */
public class BackupGridServiceManagerChangedEvent {

    public static enum Type {
        ADDED,
        REMOVED
    }

    private final ProcessingUnit processingUnit;

    private final GridServiceManager gridServiceManager;

    private final Type type;

    public BackupGridServiceManagerChangedEvent(ProcessingUnit processingUnit, Type type, GridServiceManager gridServiceManager) {
        this.processingUnit = processingUnit;
        this.type = type;
        this.gridServiceManager = gridServiceManager;
    }

    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

    public Type getType() {
        return type;
    }

    public GridServiceManager getGridServiceManager() {
        return gridServiceManager;
    }
}