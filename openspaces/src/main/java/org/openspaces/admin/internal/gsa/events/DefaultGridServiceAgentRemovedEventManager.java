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
package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgentRemovedEventManager implements InternalGridServiceAgentRemovedEventManager {

    private final InternalGridServiceAgents gridServiceAgents;

    private final InternalAdmin admin;

    private final List<GridServiceAgentRemovedEventListener> listeners = new CopyOnWriteArrayList<GridServiceAgentRemovedEventListener>();

    public DefaultGridServiceAgentRemovedEventManager(InternalGridServiceAgents gridServiceAgents) {
        this.gridServiceAgents = gridServiceAgents;
        this.admin = (InternalAdmin) gridServiceAgents.getAdmin();
    }

    public void gridServiceAgentRemoved(final GridServiceAgent gridServiceAgent) {
        for (final GridServiceAgentRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceAgentRemoved(gridServiceAgent);
                }
            });
        }
    }

    public void add(GridServiceAgentRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(GridServiceAgentRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceAgentRemovedEventListener(eventListener));
        } else {
            add((GridServiceAgentRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceAgentRemovedEventListener(eventListener));
        } else {
            remove((GridServiceAgentRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
