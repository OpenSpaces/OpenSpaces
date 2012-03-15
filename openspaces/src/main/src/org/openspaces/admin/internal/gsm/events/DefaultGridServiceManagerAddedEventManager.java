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
package org.openspaces.admin.internal.gsm.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagerAddedEventManager implements InternalGridServiceManagerAddedEventManager {

    private final InternalGridServiceManagers gridServiceManagers;

    private final InternalAdmin admin;

    private final List<GridServiceManagerAddedEventListener> listeners = new CopyOnWriteArrayList<GridServiceManagerAddedEventListener>();

    public DefaultGridServiceManagerAddedEventManager(InternalGridServiceManagers gridServiceManagers) {
        this.gridServiceManagers = gridServiceManagers;
        this.admin = (InternalAdmin) gridServiceManagers.getAdmin();
    }

    public void gridServiceManagerAdded(final GridServiceManager gridServiceManager) {
        for (final GridServiceManagerAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceManagerAdded(gridServiceManager);
                }
            });
        }
    }

    public void add(final GridServiceManagerAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (GridServiceManager gridServiceManager : gridServiceManagers) {
                        eventListener.gridServiceManagerAdded(gridServiceManager);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void add(final GridServiceManagerAddedEventListener eventListener) {
        add(eventListener, true);
    }

    public void remove(GridServiceManagerAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceManagerAddedEventListener(eventListener));
        } else {
            add((GridServiceManagerAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceManagerAddedEventListener(eventListener));
        } else {
            remove((GridServiceManagerAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
