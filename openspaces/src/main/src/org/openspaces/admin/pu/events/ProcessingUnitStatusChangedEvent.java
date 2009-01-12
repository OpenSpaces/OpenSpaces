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

import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * An event raised when a Processing Unit {@link org.openspaces.admin.pu.DeploymentStatus} has changed.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitStatusChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitStatusChanged() 
 */
public class ProcessingUnitStatusChangedEvent {

    private final ProcessingUnit processingUnit;

    private final DeploymentStatus previousStatus;

    private final DeploymentStatus newStatus;

    public ProcessingUnitStatusChangedEvent(ProcessingUnit processingUnit, DeploymentStatus previousStatus, DeploymentStatus newStatus) {
        this.processingUnit = processingUnit;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    /**
     * Returns the Processing Unit that the deployment status changed for.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

    /**
     * The previous deployment status.
     */
    public DeploymentStatus getPreviousStatus() {
        return previousStatus;
    }

    /**
     * The new deployment status.
     */
    public DeploymentStatus getNewStatus() {
        return newStatus;
    }
}
