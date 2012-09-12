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
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultReplicationStatusChangedEventManager implements InternalReplicationStatusChangedEventManager {

    private final InternalAdmin admin;

    private final List<ReplicationStatusChangedEventListener> listeners = new CopyOnWriteArrayList<ReplicationStatusChangedEventListener>();

    public DefaultReplicationStatusChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void replicationStatusChanged(final ReplicationStatusChangedEvent event) {
        for (final ReplicationStatusChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.replicationStatusChanged(event);
                }
            });
        }
    }

    public void add(final ReplicationStatusChangedEventListener eventListener) {
        //provide existing status to new listener
        for (Space space : admin.getSpaces().getSpaces()) {
            for (SpaceInstance spaceInstance : space.getInstances()) {
                for (ReplicationTarget replicationTarget : spaceInstance.getReplicationTargets()) {
                    final ReplicationStatusChangedEvent event = new ReplicationStatusChangedEvent(spaceInstance, replicationTarget, null, replicationTarget.getReplicationStatus());
                    admin.raiseEvent(eventListener, new Runnable() {
                        public void run() {
                            eventListener.replicationStatusChanged(event);
                        }
                    });
                }
            }
        }

        listeners.add(eventListener);
    }

    public void remove(ReplicationStatusChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureReplicationStatusChangedEventListener(eventListener));
        } else {
            add((ReplicationStatusChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureReplicationStatusChangedEventListener(eventListener));
        } else {
            remove((ReplicationStatusChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
