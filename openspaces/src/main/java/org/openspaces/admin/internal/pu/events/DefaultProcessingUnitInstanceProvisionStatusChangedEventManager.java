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
package org.openspaces.admin.internal.pu.events;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.pu.InternalProvisionStatusHolder;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventListener;

public class DefaultProcessingUnitInstanceProvisionStatusChangedEventManager implements
        InternalProcessingUnitInstanceProvisionStatusChangedEventManager {

    private final InternalAdmin admin;

    private final List<ProcessingUnitInstanceProvisionStatusChangedEventListener> eventListeners = new CopyOnWriteArrayList<ProcessingUnitInstanceProvisionStatusChangedEventListener>();

    private final InternalProcessingUnit processingUnit;

    public DefaultProcessingUnitInstanceProvisionStatusChangedEventManager(InternalAdmin admin) {
        this(admin, null);
    }
    
    public DefaultProcessingUnitInstanceProvisionStatusChangedEventManager(InternalAdmin admin, InternalProcessingUnit processingUnit) {
        this.admin = admin;
        this.processingUnit = processingUnit;
    }

    @Override
    public void add(ProcessingUnitInstanceProvisionStatusChangedEventListener listener) {
        add(listener, true);
    }
    
    @Override
    public void add(final ProcessingUnitInstanceProvisionStatusChangedEventListener listener, boolean includeCurrentStatus) {
        if (includeCurrentStatus) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    if (processingUnit != null) {
                        notifyListener(listener, processingUnit);
                    } else {
                        for (ProcessingUnit pu : admin.getProcessingUnits()) {
                            notifyListener(listener, pu);
                        }
                    }
                }

                private void notifyListener(final ProcessingUnitInstanceProvisionStatusChangedEventListener listener,
                        ProcessingUnit pu) {
                    Map<String, InternalProvisionStatusHolder> provisionStatusPerInstanceMap = ((InternalProcessingUnit)pu).getProvisionStatusPerInstance();
                    for (Entry<String, InternalProvisionStatusHolder> entrySet : provisionStatusPerInstanceMap.entrySet()) {
                        String processingUnitInstanceName = entrySet.getKey();
                        InternalProvisionStatusHolder statusHolder = entrySet.getValue();
                        ProcessingUnitInstance processingUnitInstance = getProcessingUnitInstanceByName(pu, processingUnitInstanceName); //may be null
                        GridServiceContainer gridServiceContainer = getGridServiceContainer(processingUnitInstance); //may be null (if instance is null)

                        listener.processingUnitInstanceProvisionStatusChanged(new ProcessingUnitInstanceProvisionStatusChangedEvent(
                                pu, processingUnitInstanceName, statusHolder.getPrevProvisionStatus(),
                                statusHolder.getNewProvisionStatus(), gridServiceContainer, processingUnitInstance));
                    }
                }
            });
        }      
        eventListeners.add(listener);
        
    }
    
    @Override
    public void remove(ProcessingUnitInstanceProvisionStatusChangedEventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void processingUnitInstanceProvisionStatusChanged(final ProcessingUnitInstanceProvisionStatusChangedEvent event) {
        for (final ProcessingUnitInstanceProvisionStatusChangedEventListener listener : eventListeners) {
            // We delay the event until the next 'flushEvents()' is called, since GSCs and processing
            // unit instances may have not been discovered yet. We make a best effort to return a 
            // value (instead of null) from the calls to event.getGridServiceContainer() and event.getProcessingUnitInstance()
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitInstanceProvisionStatusChanged(event);
                }
            });
        }
    }
    
    /**
     * Get the processing unit instance matching by instance-name
     * @return the matching instance or <code>null</code>
     */
    private ProcessingUnitInstance getProcessingUnitInstanceByName(ProcessingUnit processingUnit, String processingUnitInstanceName) {
        
        for (ProcessingUnitInstance instance : processingUnit.getInstances()) {
            if (instance.getProcessingUnitInstanceName().equals(processingUnitInstanceName)) {
                return instance;
            }
        }
        return null;
    }
    
    /**
     * Get the grid service container for this instance.
     * @param processingUnitInstance
     * @return if instance is null, return <code>null</code>
     */
    private GridServiceContainer getGridServiceContainer(ProcessingUnitInstance processingUnitInstance) {
        if (processingUnitInstance == null) {
            return null;
        } else {
            return processingUnitInstance.getGridServiceContainer();
        }
    }
}
