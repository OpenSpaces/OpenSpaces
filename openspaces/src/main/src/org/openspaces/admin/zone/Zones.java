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

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.zone.events.ZoneAddedEventManager;
import org.openspaces.admin.zone.events.ZoneLifecycleEventListener;
import org.openspaces.admin.zone.events.ZoneRemovedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface Zones extends AdminAware, Iterable<Zone> {

    Zone getByName(String name);

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
}
