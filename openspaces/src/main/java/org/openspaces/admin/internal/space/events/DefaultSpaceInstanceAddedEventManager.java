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
package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.InternalSpaceInstancesAware;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceAddedEventManager implements InternalSpaceInstanceAddedEventManager {

    private final InternalSpaceInstancesAware spaceInstances;

    private final InternalAdmin admin;

    private final List<SpaceInstanceAddedEventListener> listeners = new CopyOnWriteArrayList<SpaceInstanceAddedEventListener>();

    public DefaultSpaceInstanceAddedEventManager(InternalAdmin admin, InternalSpaceInstancesAware spaceInstances) {
        this.admin = admin;
        this.spaceInstances = spaceInstances;
    }

    public void spaceInstanceAdded(final SpaceInstance spaceInstance) {
        for (final SpaceInstanceAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceInstanceAdded(spaceInstance);
                }
            });
        }
    }

    public void add(final SpaceInstanceAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (SpaceInstance spaceInstance : spaceInstances.getSpaceInstances()) {
                        eventListener.spaceInstanceAdded(spaceInstance);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void add(final SpaceInstanceAddedEventListener eventListener) {
        add(eventListener, true);
    }

    public void remove(SpaceInstanceAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceInstanceAddedEventListener(eventListener));
        } else {
            add((SpaceInstanceAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceInstanceAddedEventListener(eventListener));
        } else {
            remove((SpaceInstanceAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
