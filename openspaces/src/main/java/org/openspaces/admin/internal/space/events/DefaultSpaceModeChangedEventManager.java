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
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceModeChangedEventManager implements InternalSpaceModeChangedEventManager {

    private final InternalAdmin admin;

    private final InternalSpaceInstancesAware spaceInstances;

    private final List<SpaceModeChangedEventListener> listeners = new CopyOnWriteArrayList<SpaceModeChangedEventListener>();

    public DefaultSpaceModeChangedEventManager(InternalSpaceInstancesAware spaceInstances, InternalAdmin admin) {
        this.spaceInstances = spaceInstances;
        this.admin = admin;
    }

    public void spaceModeChanged(final SpaceModeChangedEvent event) {
        for (final SpaceModeChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceModeChanged(event);
                }
            });
        }
    }

    public void add(final SpaceModeChangedEventListener eventListener) {
        if (spaceInstances != null) {
            SpaceInstance[] instances = spaceInstances.getSpaceInstances();
            listeners.add(eventListener);
            for (final SpaceInstance spaceInstance : instances) {
                admin.raiseEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.spaceModeChanged(new SpaceModeChangedEvent(spaceInstance, null, spaceInstance.getMode()));
                    }
                });
            }
        } else {
            listeners.add(eventListener);
        }
    }

    public void remove(SpaceModeChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceModeChangedEventListener(eventListener));
        } else {
            add((SpaceModeChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceModeChangedEventListener(eventListener));
        } else {
            remove((SpaceModeChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
