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
package org.openspaces.admin.pu;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionFailure;

/**
 * The provision status of a processing unit instance. When a processing unit is deployed/maintained, each instance 
 * will be in one of the following transition states.
 * <ul>
 * <li>{@link #ATTEMPT} - an attempt to provision an instance on an available {@link GridServiceContainer}</li>
 * <li>{@link #SUCCESS} - a successful provisioning attempt on a {@link GridServiceContainer}</li>
 * <li>{@link #FAILURE} - a failed attempt to provision an instance on an available {@link GridServiceContainer}</li>
 * <li>{@link #PENDING} - a pending to provision an instance until a matching {@link GridServiceContainer} is discovered</li>
 * </ul>
 * 
 * @see org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEvent
 * @see org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventListener
 * 
 * @since 8.0.6
 * @author moran
 */
public enum ProvisionStatus {
    ATTEMPT,
    SUCCESS,
    FAILURE,
    PENDING;
    
    /**
     * IS SET USING REFLECTION!
     * @see org.openspaces.admin.internal.pu.DefaultProcessingUnit#processProvisionEvent(org.jini.rio.monitor.ProvisionLifeCycleEvent)
     */
    private ProcessingUnitInstanceProvisionFailure provisionFailure; //set using reflection!

    /**
     * @return Upon a provision {@link #FAILURE}, the reason/cause can be extracted. Otherwise returns <code>null</code>.
     */
    public ProcessingUnitInstanceProvisionFailure getProvisionFailure() {
        return provisionFailure;
    }
}
