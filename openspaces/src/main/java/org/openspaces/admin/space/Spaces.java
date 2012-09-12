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

package org.openspaces.admin.space;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.space.events.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Spaces holds all the currently discovered {@link Space}s.
 *
 * <p>Provides simple means to get all the current Space, as well as as registering for
 * Space lifecycle (added and removed) events.
 *
 * <p>Provides the ability to start a statistics monitor on all current Space using
 * {@link #startStatisticsMonitor()}. Newly discovered Space will automatically use
 * the statistics monitor as well.
 *
 * @author kimchy
 */
public interface Spaces extends Iterable<Space>, AdminAware, StatisticsMonitor {

    /**
     * Returns all the currently discovered {@link org.openspaces.admin.space.Space}s.
     */
    Space[] getSpaces();

    /**
     * Returns a space based on its uid.
     */
    Space getSpaceByUID(String uid);

    /**
     * Returns a space based on its name.
     */
    Space getSpaceByName(String name);

    /**
     * Returns a map of {@link org.openspaces.admin.space.Space}s keyed by their names.
     */
    Map<String, Space> getNames();

    /**
     * Waits indefinitely till the provided Space name is discovered.
     *
     * @param spaceName The space name to wait for
     */
    Space waitFor(String spaceName);

    /**
     * Waits for the given timeout (in time unit) till the space name is discovered.
     * Returns <code>true</code> if the space is discovered, <code>false</code>
     * if the timeout expired.
     */
    Space waitFor(String spaceName, long timeout, TimeUnit timeUnit);

    /**
     * Returns an event manager allowing to add {@link org.openspaces.admin.space.events.SpaceAddedEventListener}s.
     */
    SpaceAddedEventManager getSpaceAdded();

    /**
     * Returns an event manager allowing to remove {@link org.openspaces.admin.space.events.SpaceAddedEventListener}s.
     */
    SpaceRemovedEventManager getSpaceRemoved();

    /**
     * Adds a {@link org.openspaces.admin.space.events.SpaceLifecycleEventListener} to be notified
     * when a {@link org.openspaces.admin.space.Space} is added and removed.
     */
    void addLifecycleListener(SpaceLifecycleEventListener eventListener);

    /**
     * Removes a {@link org.openspaces.admin.space.events.SpaceLifecycleEventListener} to be notified
     * when a {@link org.openspaces.admin.space.Space} is added and removed.
     */
    void removeLifecycleListener(SpaceLifecycleEventListener eventListener);

    /**
     * Returns an event manager allowing to globally add {@link org.openspaces.admin.space.events.SpaceInstanceAddedEventListener}
     * that will be called for any space instance discovered.
     */
    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    /**
     * Returns an event manager allowing to globally remove {@link org.openspaces.admin.space.events.SpaceInstanceAddedEventListener}
     * that will be called for any space instance discovered.
     */
    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    /**
     * Allows to add a {@link org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener} hthat will be called
     * for any space instance discovered.
     */
    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener} hthat will be called
     * for any space instance discovered.
     */
    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Returns an event manager allowing to globally register for {@link org.openspaces.admin.space.events.SpaceModeChangedEvent}s
     * that happen on any Space instance currently discovered.
     */
    SpaceModeChangedEventManager getSpaceModeChanged();

    /**
     * Returns an event manager allowing to globally register for {@link org.openspaces.admin.space.events.ReplicationStatusChangedEvent}s
     * that happen on any Space instance currently discovered.
     */
    ReplicationStatusChangedEventManager getReplicationStatusChanged();

    /**
     * Returns an event manager allowing to register for {@link org.openspaces.admin.space.events.SpaceStatisticsChangedEvent}s
     * that occur on all the currently discovered {@link org.openspaces.admin.space.Space}s.
     *
     * <p>Note, {@link #startStatisticsMonitor()} must be called in order to start monitor statistics.
     */
    SpaceStatisticsChangedEventManager getSpaceStatisticsChanged();

    /**
     * Returns an event manager allowing to register for {@link org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent}s
     * that occur on all the currently discovered {@link org.openspaces.admin.space.SpaceInstance}s.
     *
     * <p>Note, {@link #startStatisticsMonitor()} must be called in order to start monitoring statistics.
     */
    SpaceInstanceStatisticsChangedEventManager getSpaceInstanceStatisticsChanged();
}
