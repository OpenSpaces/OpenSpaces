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

package org.openspaces.admin.zone;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.zone.events.ZoneAddedEventManager;
import org.openspaces.admin.zone.events.ZoneLifecycleEventListener;
import org.openspaces.admin.zone.events.ZoneRemovedEventManager;

/**
 * @author kimchy
 */
public interface Zones extends AdminAware, Iterable<Zone> {

    /**
     * Returns all currently discovered zones.
     */
    Zone[] getZones();
    
    /**
     * Returns a discovered zone by its name.
     * 
     * @param name The name of the zone to match.
     * @return The discovered zone; or <code>null</code> if no match has yet been discovered.
     */
    Zone getByName(String name);

    /**
     * Returns a map of zone names to {@link Zone}s.
     * @return a map holding all discovered zones.
     */
    Map<String, Zone> getNames();

    /**
     * Returns the machines added event manager allowing to add and remove
     * {@link org.openspaces.admin.zone.events.ZoneAddedEventListener}s.
     */
    ZoneAddedEventManager getZoneAdded();

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.zone.events.ZoneRemovedEventListener}s.
     */
    ZoneRemovedEventManager getZoneRemoved();

    /**
     * Allows to add a {@link ZoneLifecycleEventListener}.
     */
    void addLifecycleListener(ZoneLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ZoneLifecycleEventListener}.
     */
    void removeLifeycleListener(ZoneLifecycleEventListener eventListener);
    
    /**
     * Waits indefinitely till the zone with the given name is discovered.
     *
     * @param zoneByName The zone name
     * @return The discovered zone
     */
    Zone waitFor(String zoneByName);

    /**
     * Waits for the given timeout (in time unit) till the zone with the given name is discovered.
     * Returns the zone if it was discovered within the provided timeout, or <code>null</code> if
     * the zone was not discovered.
     * 
     * @param zoneByName The zone name
     * @return the zone if it was discovered within the provided timeout, or <code>null</code> if
     *         the zone was not discovered.
     */
    Zone waitFor(String zoneByName, long timeout, TimeUnit timeUnit);
}
