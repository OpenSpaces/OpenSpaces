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

package org.openspaces.admin.gsc;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventManager;
import org.openspaces.admin.gsc.events.GridServiceContainerLifecycleEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Grid Service Containers hold all the different {@link GridServiceContainer}s that are currently
 * discovered.
 *
 * <p>Provides simple means to get all the current containers, as well as as registering for
 * container lifecycle (added and removed) events.
 * 
 * @author kimchy
 */
public interface GridServiceContainers extends AdminAware, Iterable<GridServiceContainer> {

    /**
     * Returns all the currently discovered containers.
     */
    GridServiceContainer[] getContainers();

    /**
     * Returns a container based on its uid.
     *
     * @see GridServiceContainer#getUid()
     */
    GridServiceContainer getContainerByUID(String uid);

    /**
     * Returns a map of grid service container with the key as the uid.
     */
    Map<String, GridServiceContainer> getUids();

    /**
     * Returns the number of containers current discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no containers, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Waits indefinitely till the provided number of containers are up.
     *
     * @param numberOfGridServiceContainers The number of containers to wait for
     */
    boolean waitFor(int numberOfGridServiceContainers);

    /**
     * Waits for the given timeout (in time unit) till the provided number of containers are up.
     * Returns <code>true</code> if the required number of containers were discovered, <code>false</code>
     * if the timeout expired.
     *
     * @param numberOfGridServiceContainers The number of containers to wait for
     */
    boolean waitFor(int numberOfGridServiceContainers, long timeout, TimeUnit timeUnit);

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener}s.
     */
    GridServiceContainerAddedEventManager getGridServiceContainerAdded();

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener}s.
     */
    GridServiceContainerRemovedEventManager getGridServiceContainerRemoved();

    /**
     * Allows to add a {@link GridServiceContainerLifecycleEventListener}.
     */
    void addLifecycleListener(GridServiceContainerLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link GridServiceContainerLifecycleEventListener}.
     */
    void removeLifecycleListener(GridServiceContainerLifecycleEventListener eventListener);
}