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

import org.openspaces.admin.pu.MemberAliveIndicatorStatus;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event raised when a processing unit instance {@link MemberAliveIndicatorStatus} has changed.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnitInstance#getMemberAliveIndicatorStatus()
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceMemberAliveIndicatorStatusChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceMemberAliveIndicatorStatusChanged()
 * 
 * @since 8.0.6
 * @author moran
 */
public class ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent {
    
    private final ProcessingUnitInstance processingUnitInstance;
    
    private final MemberAliveIndicatorStatus previousStatus;
    
    private final MemberAliveIndicatorStatus newStatus;
    
    public ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent(ProcessingUnitInstance processingUnitInstance, MemberAliveIndicatorStatus previousStatus, MemberAliveIndicatorStatus newStatus) {
        this.processingUnitInstance = processingUnitInstance;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
    
    /**
     * @return The processing unit instance for which the member alive indicator status changed for.
     */
    public ProcessingUnitInstance getProcessingUnitInstance() {
        return processingUnitInstance;
    }
    
    /**
     * @return The previous member alive indicator status. May be <code>null</code> for newly registered listeners.
     */
    public MemberAliveIndicatorStatus getPreviousStatus() {
        return previousStatus;
    }
    
    /**
     * @see ProcessingUnitInstance#getMemberAliveIndicatorStatus()
     * @return The new member alive indicator status.
     */
    public MemberAliveIndicatorStatus getNewStatus() {
        return newStatus;
    }
}
