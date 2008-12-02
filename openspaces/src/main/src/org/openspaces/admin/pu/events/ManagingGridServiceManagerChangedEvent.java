package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author kimchy
 */
public class ManagingGridServiceManagerChangedEvent {

    private final ProcessingUnit processingUnit;

    private final GridServiceManager newGridServiceManager;

    private final GridServiceManager previousGridServiceManager;

    public ManagingGridServiceManagerChangedEvent(ProcessingUnit processingUnit, GridServiceManager newGridServiceManager, GridServiceManager previousGridServiceManager) {
        this.processingUnit = processingUnit;
        this.newGridServiceManager = newGridServiceManager;
        this.previousGridServiceManager = previousGridServiceManager;
    }

    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

    public GridServiceManager getNewGridServiceManager() {
        return newGridServiceManager;
    }

    public GridServiceManager getPreviousGridServiceManager() {
        return previousGridServiceManager;
    }

    /**
     * Returns <code>true</code> if there is unknown managing grid service manager.
     */
    public boolean isUnknown() {
        return newGridServiceManager == null;
    }
}
