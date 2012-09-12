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
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitAddedEventManager implements InternalProcessingUnitAddedEventManager {

    private final InternalProcessingUnits processingUnits;

    private final InternalAdmin admin;

    private final List<ProcessingUnitAddedEventListener> listeners = new CopyOnWriteArrayList<ProcessingUnitAddedEventListener>();

    public DefaultProcessingUnitAddedEventManager(InternalProcessingUnits processingUnits) {
        this.processingUnits = processingUnits;
        this.admin = (InternalAdmin) processingUnits.getAdmin();
    }

    public void processingUnitAdded(final ProcessingUnit processingUnit) {
        for (final ProcessingUnitAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitAdded(processingUnit);
                }
            });
        }
    }

    public void add(final ProcessingUnitAddedEventListener eventListener) {
        add(eventListener, true);
    }
    
    public void add(final ProcessingUnitAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (ProcessingUnit processingUnit : processingUnits) {
                        eventListener.processingUnitAdded(processingUnit);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void remove(ProcessingUnitAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitAddedEventListener(eventListener));
        } else {
            add((ProcessingUnitAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitAddedEventListener(eventListener));
        } else {
            remove((ProcessingUnitAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
