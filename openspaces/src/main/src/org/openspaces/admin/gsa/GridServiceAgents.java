/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.gsa;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventManager;
import org.openspaces.admin.gsa.events.GridServiceAgentLifecycleEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Grid Service Agents hold all the different {@link GridServiceAgent} that are currently
 * discovered.
 *
 * <p>Provides simple means to get all the current agents, as well as as registering for
 * agent lifecycle (added and removed) events.
 *
 * @author kimchy
 */
public interface GridServiceAgents extends AdminAware, Iterable<GridServiceAgent> {

    /**
     * Returns all the currently discovered agents.
     */
    GridServiceAgent[] getAgents();

    /**
     * Returns an agent based on its uid.
     *
     * @see GridServiceAgent#getUid()
     */
    GridServiceAgent getAgentByUID(String uid);

    /**
     * Returns a map of grid service agent with the key as the uid.
     */
    Map<String, GridServiceAgent> getUids();

    /**
     * Returns a map of grid service agent with the key as the host address it is running
     * on.
     */
    Map<String, GridServiceAgent> getHostAddress();

    /**
     * Returns a map of grid service agent with the key as the host name it is running
     * on.
     */
    Map<String, GridServiceAgent> getHostNames();

    /**
     * Returns the number of agents current discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no agents, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Waits indefinitely till at least one agent is discovered.
     */
    GridServiceAgent waitForAtLeastOne();

    /**
     * Waits for the given timeout (in time unit) till at least one agent is discovered.
     */
    GridServiceAgent waitForAtLeastOne(long timeout, TimeUnit timeUnit);

    /**
     * Waits indefinitely till the provided number of agents are up.
     *
     * @param numberOfAgents The number of agents to wait for
     */
    boolean waitFor(int numberOfAgents);

    /**
     * Waits for the given timeout (in time unit) till the provided number of agents are up.
     *
     * @param numberOfAgents The number of agents to wait for
     */
    boolean waitFor(int numberOfAgents, long timeout, TimeUnit timeUnit);

    /**
     * Returns the grid service agent added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener}s.
     */
    GridServiceAgentAddedEventManager getGridServiceAgentAdded();

    /**
     * Returns the grid service agent added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener}s.
     */
    GridServiceAgentRemovedEventManager getGridServiceAgentRemoved();

    /**
     * Allows to add a {@link GridServiceAgentLifecycleEventListener}.
     */
    void addLifecycleListener(GridServiceAgentLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link GridServiceAgentLifecycleEventListener}.
     */
    void removeLifecycleListener(GridServiceAgentLifecycleEventListener eventListener);
}
