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

package org.openspaces.admin.lus;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.lus.events.LookupServiceAddedEventManager;
import org.openspaces.admin.lus.events.LookupServiceLifecycleEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Lookup Services hold all the different {@link LookupService}s that are currently
 * discovered.
 *
 * <p>Provides simple means to get all the current lus, as well as as registering for
 * lus lifecycle (added and removed) events.
 *
 * @author kimchy
 */
public interface LookupServices extends AdminAware, Iterable<LookupService>, DumpProvider {

    /**
     * Returns all the currently discovered lus.
     */
    LookupService[] getLookupServices();

    /**
     * Returns a lus based on its uid.
     *
     * @see LookupService#getUid()
     */
    LookupService getLookupServiceByUID(String id);

    /**
     * Returns a map of lookup service with the key as the uid.
     */
    Map<String, LookupService> getUids();

    /**
     * Returns the number of lookup services current discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no containers, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Waits indefinitely till the provided number of lookup services are up.
     *
     * @param numberOfLookupServices The number of lookup services to wait for
     */
    boolean waitFor(int numberOfLookupServices);

    /**
     * Waits for the given timeout (in time unit) till the provided number of lookup services are up.
     * Returns <code>true</code> if the required number of lookup services were discovered, <code>false</code>
     * if the timeout expired.
     *
     * @param numberOfLookupServices The number of lookup services to wait for
     */
    boolean waitFor(int numberOfLookupServices, long timeout, TimeUnit timeUnit);

    /**
     * Returns the lookup service added event manager allowing to add and remove
     * {@link org.openspaces.admin.lus.events.LookupServiceAddedEventListener}s.
     */
    LookupServiceAddedEventManager getLookupServiceAdded();

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.lus.events.LookupServiceRemovedEventManager}s.
     */
    LookupServiceRemovedEventManager getLookupServiceRemoved();

    /**
     * Allows to add a {@link LookupServiceLifecycleEventListener}.
     */
    void addLifecycleListener(LookupServiceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link LookupServiceLifecycleEventListener}.
     */
    void removeLifecycleListener(LookupServiceLifecycleEventListener eventListener);
}
