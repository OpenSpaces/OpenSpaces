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
package org.openspaces.admin.internal.gsc;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;

import com.gigaspaces.grid.gsc.GSC;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer, InternalAgentGridComponent {

    ServiceID getServiceID();

    GSC getGSC();

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);
    
    /**
     * @return false only when all processing unit instances have completed shutdown.
     * @see ProcessingUnitInstanceRemovedEventListener - use this event to get an indication when processing unit instance has started shutdown.
     * @since 8.0.4
     */
    boolean hasProcessingUnitInstances();
    
    /**
     * @return UIDs of instances that have been removed from the lookup service/Admin API, but are still reported by the GSC
     * @since 9.0.1
     */
    String[] getUnconfirmedRemovedProcessingUnitInstancesUid();
}
