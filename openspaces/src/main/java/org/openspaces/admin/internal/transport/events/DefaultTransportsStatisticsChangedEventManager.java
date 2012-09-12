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
package org.openspaces.admin.internal.transport.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultTransportsStatisticsChangedEventManager implements InternalTransportsStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<TransportsStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<TransportsStatisticsChangedEventListener>();

    public DefaultTransportsStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void transportsStatisticsChanged(final TransportsStatisticsChangedEvent event) {
        for (final TransportsStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.transportsStatisticsChanged(event);
                }
            });
        }
    }

    public void add(TransportsStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(TransportsStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureTransportsStatisticsChangedEventListener(eventListener));
        } else {
            add((TransportsStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureTransportsStatisticsChangedEventListener(eventListener));
        } else {
            remove((TransportsStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
