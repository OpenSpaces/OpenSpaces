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
 * An event indicating that a backup GSM of a processing unit was either added or removed.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnit#getBackupGridServiceManagerChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getBackupGridServiceManagerChanged()
 */
public class BackupGridServiceManagerChangedEvent {

    /**
     * The type of the event.
     */
    public static enum Type {
        /**
         * The event indicates that a GSM backup was added.
         */
        ADDED,
        /**
         * The event indicates that a GSM backup was removed.
         */
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

    /**
     * Returns the Processing Unit that the backup GSM event occured on.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

    /**
     * Returns the type of the event.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the backup GSM that was either added or removed for the given Processing Unit.
     */
    public GridServiceManager getGridServiceManager() {
        return gridServiceManager;
    }
}