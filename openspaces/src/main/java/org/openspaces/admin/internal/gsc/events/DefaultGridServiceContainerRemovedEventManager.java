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
package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainerRemovedEventManager implements InternalGridServiceContainerRemovedEventManager {

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalAdmin admin;

    private final List<GridServiceContainerRemovedEventListener> listeners = new CopyOnWriteArrayList<GridServiceContainerRemovedEventListener>();

    public DefaultGridServiceContainerRemovedEventManager(InternalGridServiceContainers gridServiceContainers) {
        this.gridServiceContainers = gridServiceContainers;
        this.admin = (InternalAdmin) gridServiceContainers.getAdmin();
    }

    public void gridServiceContainerRemoved(final GridServiceContainer gridServiceContainer) {
        for (final GridServiceContainerRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceContainerRemoved(gridServiceContainer);
                }
            });
        }
    }

    public void add(GridServiceContainerRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(GridServiceContainerRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceContainerRemovedEventListener(eventListener));
        } else {
            add((GridServiceContainerRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceContainerRemovedEventListener(eventListener));
        } else {
            remove((GridServiceContainerRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
