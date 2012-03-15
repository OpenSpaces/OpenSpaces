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
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstanceStatisticsChangedEventManager implements InternalProcessingUnitInstanceStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<ProcessingUnitInstanceStatisticsChangedEventListener> eventListeners = new CopyOnWriteArrayList<ProcessingUnitInstanceStatisticsChangedEventListener>();

    public DefaultProcessingUnitInstanceStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void add(ProcessingUnitInstanceStatisticsChangedEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void remove(ProcessingUnitInstanceStatisticsChangedEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void processingUnitInstanceStatisticsChanged(final ProcessingUnitInstanceStatisticsChangedEvent event) {
        for (final ProcessingUnitInstanceStatisticsChangedEventListener listener : eventListeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitInstanceStatisticsChanged(event);
                }
            });
        }
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitInstanceStatisticsChangedEventListener(eventListener));
        } else {
            add((ProcessingUnitInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitInstanceStatisticsChangedEventListener(eventListener));
        } else {
            remove((ProcessingUnitInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
