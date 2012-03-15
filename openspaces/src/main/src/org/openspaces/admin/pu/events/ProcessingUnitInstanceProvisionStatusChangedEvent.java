/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProvisionStatus;

/**
 * An event raised when a processing unit instance {@link ProvisionStatus} has changed.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionStatusChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionStatusChanged()
 * 
 * @since 8.0.6
 * @author moran
 */
public class ProcessingUnitInstanceProvisionStatusChangedEvent {
    
    private final ProcessingUnit processingUnit;
    private final String processingUnitInstanceName;
    private final ProvisionStatus previousStatus;
    private final ProvisionStatus newStatus;
    private final String gscServiceId;

    private GridServiceContainer cachedGridServiceContainer; //may be null on pending
    private ProcessingUnitInstance cachedProcessingUnitInstance; //may be null on pending/failure
    
    public ProcessingUnitInstanceProvisionStatusChangedEvent(ProcessingUnit processingUnit,
            String processingUnitInstanceName, ProvisionStatus previousStatus, ProvisionStatus newStatus,
            GridServiceContainer gridServiceContainer, ProcessingUnitInstance processingUnitInstance) {
        this.processingUnit = processingUnit;
        this.processingUnitInstanceName = processingUnitInstanceName;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.cachedGridServiceContainer = gridServiceContainer;
        this.cachedProcessingUnitInstance = processingUnitInstance;
        this.gscServiceId = null;
    }
    
    public ProcessingUnitInstanceProvisionStatusChangedEvent(ProcessingUnit processingUnit,
            String processingUnitInstanceName, ProvisionStatus previousStatus, ProvisionStatus newStatus,
            String gscServiceId) {
        this.processingUnit = processingUnit;
        this.processingUnitInstanceName = processingUnitInstanceName;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.gscServiceId = gscServiceId;
        this.cachedGridServiceContainer = null;
        this.cachedProcessingUnitInstance = null;
    }
    
    /**
     * @return The processing unit this event refers to.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }
    
    /**
     * @return The processing unit instance name this event refers to.
     * @see ProcessingUnitInstance#getProcessingUnitInstanceName().
     */
    public String getProcessingUnitInstanceName() {
        return processingUnitInstanceName;
    }
     
    /**
     * @return The previous provision status or <code>null</code> if not status was recorded.
     */
    public ProvisionStatus getPreviousStatus() {
        return previousStatus;
    }
    
    /**
     * @return The current (new) provision status for the specified processing unit instance.
     */
    public ProvisionStatus getNewStatus() {
        return newStatus;
    }
    
    /**
     * For {@link ProvisionStatus#ATTEMPT} - returns the {@link GridServiceContainer} a processing unit instance is instantiating on. May be <code>null</code> if not yet discovered.
     * For {@link ProvisionStatus#SUCCESS} - returns the {@link GridServiceContainer} a processing unit instance has successfully instantiated on. May be <code>null</code> if not yet discovered.
     * For {@link ProvisionStatus#FAILURE} - returns the {@link GridServiceContainer} a processing unit instance has failed to instantiate on. May be <code>null</code> if no longer available.
     * For {@link ProvisionStatus#PENDING} - returns <code>null</code>.
     * 
     * @return The Grid Service Container this provision change refers to. May be <code>null</code>.
     */
    public GridServiceContainer getGridServiceContainer() {
        if (cachedGridServiceContainer == null && gscServiceId != null) {
            cachedGridServiceContainer =  processingUnit.getAdmin().getGridServiceContainers().getContainerByUID(gscServiceId);
        }
        return cachedGridServiceContainer;
    }
    
    /**
     * For {@link ProvisionStatus#ATTEMPT} - returns <code>null</code> until the processing unit instance is discovered and added.
     * For {@link ProvisionStatus#SUCCESS} - returns the {@link ProcessingUnitInstance} that has successfully instantiated. May be <code>null</code> if not yet discovered.
     * For {@link ProvisionStatus#FAILURE} - returns <code>null</code>.
     * For {@link ProvisionStatus#PENDING} - returns <code>null</code>.
     * 
     * @return The Processing Unit Instance (extracted by name) this provision change refers to. May be <code>null</code>.
     */
    public ProcessingUnitInstance getProcessingUnitInstance() {
        if (cachedProcessingUnitInstance == null) {
            for (ProcessingUnitInstance instance : processingUnit.getInstances()) {
                if (processingUnitInstanceName.equals(instance.getProcessingUnitInstanceName())) {
                    cachedProcessingUnitInstance = instance;
                    return instance;
                }
            }
        }
        return cachedProcessingUnitInstance;
    }
}
