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
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitRemovedEventManager implements InternalProcessingUnitRemovedEventManager {

    private final InternalProcessingUnits processingUnits;

    private final InternalAdmin admin;

    private final List<ProcessingUnitRemovedEventListener> listeners = new CopyOnWriteArrayList<ProcessingUnitRemovedEventListener>();

    public DefaultProcessingUnitRemovedEventManager(InternalProcessingUnits processingUnits) {
        this.processingUnits = processingUnits;
        this.admin = (InternalAdmin) processingUnits.getAdmin();
    }

    public void processingUnitRemoved(final ProcessingUnit processingUnit) {
        for (final ProcessingUnitRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitRemoved(processingUnit);
                }
            });
        }
    }

    public void add(ProcessingUnitRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(ProcessingUnitRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitRemovedEventListener(eventListener));
        } else {
            add((ProcessingUnitRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitRemovedEventListener(eventListener));
        } else {
            remove((ProcessingUnitRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
