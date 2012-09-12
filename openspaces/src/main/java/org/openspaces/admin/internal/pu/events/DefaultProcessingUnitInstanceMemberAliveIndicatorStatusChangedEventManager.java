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
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;

public class DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager implements
        InternalProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager {

    private final InternalAdmin admin;

    private final List<ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener> eventListeners = new CopyOnWriteArrayList<ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener>();

    private final InternalProcessingUnit processingUnit;

    public DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager(InternalAdmin admin) {
        this(admin, null);
    }
    
    public DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager(InternalAdmin admin, InternalProcessingUnit processingUnit) {
        this.admin = admin;
        this.processingUnit = processingUnit;
    }

    
    @Override
    public void add(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener) {
        add(listener, true);
    }
    
    @Override
    public void add(final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener, boolean includeCurrentStatus) {
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

                private void notifyListener(
                        final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener,
                        ProcessingUnit pu) {
                    for (ProcessingUnitInstance instance : pu) {
                        listener.processingUnitInstanceMemberAliveIndicatorStatusChanged(new ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent(
                                instance, null, instance.getMemberAliveIndicatorStatus()));
                    }
                }
            });
        }      
        eventListeners.add(listener);
    }

    @Override
    public void remove(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void processingUnitInstanceMemberAliveIndicatorStatusChanged(final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event) {
        for (final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener : eventListeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitInstanceMemberAliveIndicatorStatusChanged(event);
                }
            });
        }
    }
}
