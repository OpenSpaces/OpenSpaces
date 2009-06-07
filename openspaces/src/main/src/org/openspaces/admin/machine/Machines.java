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

package org.openspaces.admin.machine;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.machine.events.MachineAddedEventManager;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Machines hold all the different {@link Machine}s that are currently
 * discovered.
 * <p/>
 * <p>Provides simple means to get all the current machines, as well as as registering for
 * machine lifecycle (added and removed) events.
 *
 * @author kimchy
 */
public interface Machines extends AdminAware, Iterable<Machine> {

    /**
     * Returns all currently discovered machines.
     */
    Machine[] getMachines();

    /**
     * Returns the machine by the host address.
     *
     * @param hostAddress The host address to lookup the machine by
     * @return The machine correlated to the specified host address, <code>null</code> if there is no one
     */
    Machine getMachineByHostAddress(String hostAddress);

    /**
     * Returns a map of machines with the key as the uid.
     */
    Map<String, Machine> getUids();

    /**
     * Returns a map of machines by host address.
     */
    Map<String, Machine> getHostsByAddress();

    /**
     * Returns a map of machines by host names.
     */
    Map<String, Machine> getHostsByName();

    /**
     * Returns the number of machines current discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no machines, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Waits indefinitely till the provided number of machines are up.
     *
     * @param numberOfMachines The number of containers to wait for
     */
    boolean waitFor(int numberOfMachines);

    /**
     * Waits for the given timeout (in time unit) till the provided number of machines are up.
     * Returns <code>true</code> if the required number of machines were discovered, <code>false</code>
     * if the timeout expired.
     *
     * @param numberOfMachines The number of containers to wait for
     */
    boolean waitFor(int numberOfMachines, long timeout, TimeUnit timeUnit);

    /**
     * Waits indefinitely till the machine with the given host (name/address) is discovered.
     *
     * @param host The machine host name or address
     * @return The machine discovered
     */
    Machine waitFor(String host);

    /**
     * Waits for the given timeout (in time unit) till the machien with the given host (name/address)
     * is discovered. Returns the machine if it was discovered within the provided timeout, or <code>null</code>
     * if the machine was not discovered.
     *
     * @param host     The machine host name or address
     * @param timeout  The timeout value to wait
     * @param timeUnit The time unit of the timeout value
     * @return The machien discovered, or <code>null</code> if it was not discovered within the provided timeout
     */
    Machine waitFor(String host, long timeout, TimeUnit timeUnit);

    /**
     * Returns the machines added event manager allowing to add and remove
     * {@link org.openspaces.admin.machine.events.MachineAddedEventListener}s.
     */
    MachineAddedEventManager getMachineAdded();

    /**
     * Returns the grid service container added event manager allowing to add and remove
     * {@link org.openspaces.admin.machine.events.MachineRemovedEventListener}s.
     */
    MachineRemovedEventManager getMachineRemoved();

    /**
     * Allows to add a {@link MachineLifecycleEventListener}.
     */
    void addLifecycleListener(MachineLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link MachineLifecycleEventListener}.
     */
    void removeLifeycleListener(MachineLifecycleEventListener eventListener);
}
