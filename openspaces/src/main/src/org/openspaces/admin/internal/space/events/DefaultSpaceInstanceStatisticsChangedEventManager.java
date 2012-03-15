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
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceStatisticsChangedEventManager implements InternalSpaceInstanceStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<SpaceInstanceStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<SpaceInstanceStatisticsChangedEventListener>();

    public DefaultSpaceInstanceStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void spaceInstanceStatisticsChanged(final SpaceInstanceStatisticsChangedEvent event) {
        for (final SpaceInstanceStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceInstanceStatisticsChanged(event);
                }
            });
        }
    }

    public void add(SpaceInstanceStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(SpaceInstanceStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceInstanceStatisticsChangedEventListener(eventListener));
        } else {
            add((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceInstanceStatisticsChangedEventListener(eventListener));
        } else {
            remove((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
