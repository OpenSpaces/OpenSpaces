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
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceRemovedEventManager implements InternalSpaceInstanceRemovedEventManager {

    private final InternalAdmin admin;

    private final List<SpaceInstanceRemovedEventListener> listeners = new CopyOnWriteArrayList<SpaceInstanceRemovedEventListener>();

    public DefaultSpaceInstanceRemovedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void spaceInstanceRemoved(final SpaceInstance spaceInstance) {
        for (final SpaceInstanceRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceInstanceRemoved(spaceInstance);
                }
            });
        }
    }

    public void add(final SpaceInstanceRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(SpaceInstanceRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureInstanceSpaceRemovedEventListener(eventListener));
        } else {
            add((SpaceInstanceRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureInstanceSpaceRemovedEventListener(eventListener));
        } else {
            remove((SpaceInstanceRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
