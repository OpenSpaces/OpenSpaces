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

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventListener;
import org.openspaces.admin.space.Space;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitSpaceCorrelatedEventManager implements InternalProcessingUnitSpaceCorrelatedEventManager {

    private final InternalAdmin admin;

    private final InternalProcessingUnit processingUnit;

    private final List<ProcessingUnitSpaceCorrelatedEventListener> listeners = new CopyOnWriteArrayList<ProcessingUnitSpaceCorrelatedEventListener>();

    public DefaultProcessingUnitSpaceCorrelatedEventManager(InternalProcessingUnit processingUnit) {
        this.processingUnit = processingUnit;
        this.admin = (InternalAdmin) processingUnit.getAdmin();
    }

    public void processingUnitSpaceCorrelated(final ProcessingUnitSpaceCorrelatedEvent event) {
        for (final ProcessingUnitSpaceCorrelatedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitSpaceCorrelated(event);
                }
            });
        }
    }

    public void add(final ProcessingUnitSpaceCorrelatedEventListener eventListener) {
        for (final Space space : processingUnit.getSpaces()) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    eventListener.processingUnitSpaceCorrelated(new ProcessingUnitSpaceCorrelatedEvent(space, processingUnit));
                }
            });
        }
        listeners.add(eventListener);
    }

    public void remove(ProcessingUnitSpaceCorrelatedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitSpaceCorrelatedEventListener(eventListener));
        } else {
            add((ProcessingUnitSpaceCorrelatedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitSpaceCorrelatedEventListener(eventListener));
        } else {
            remove((ProcessingUnitSpaceCorrelatedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
