/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * An event indicating that a managing GSM of a processing unit was changed.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnit#getManagingGridServiceManagerChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getManagingGridServiceManagerChanged()
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

    /**
     * Returns the processing unit the managing GSM was changed for.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

    /**
     * Returns the new GSM that is associated with the processing unit.
     */
    public GridServiceManager getNewGridServiceManager() {
        return newGridServiceManager;
    }

    /**
     * Returns the previous GSM that is associated with the processing unit.
     */
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
