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

import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.machine.Machines;

/**
 * @author kimchy
 */
public interface Zone extends DumpProvider {

    /**
     * Returns name of the zone.
     */
    String getName();

    /**
     * Returns the machines running within the zone.
     */
    Machines getMachines();

    /**
     * Returns the lookup services that are running within the zone.
     */
    LookupServices getLookupServices();

    /**
     * Returns the grid service agents running within the zone.
     */
    GridServiceAgents getGridServiceAgents();

    /**
     * Returns the grid service managers running within the zone.
     */
    GridServiceManagers getGridServiceManagers();

    /**
     * Returns the grid service containers running within the zone.
     */
    GridServiceContainers getGridServiceContainers();

    /**
     * Returns the virtual machines running within the zone.
     */
    VirtualMachines getVirtualMachines();

    /**
     * Returns <code>true</code> if there are grid components.
     */
    boolean hasGridComponents();

    /**
     * Returns the transports "running"  within the zone.
     */
    Transports getTransports();

    /**
     * Returns all the processing unit instances running  within the zone.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances();

    /**
     * Returns the processing unit instance added event manager allowing to add and remove
     * {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener}s.
     */
    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    /**
     * Returns the processing unit instance removed event manager allowing to add and remove
     * {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener}s.
     */
    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    /**
     * Allows to add a {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener}.
     */
    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Returns all the space instances running within the zone.
     */
    SpaceInstance[] getSpaceInstances();

    /**
     * Returns the space instance added event manager allowing to add and remove
     * {@link org.openspaces.admin.space.events.SpaceInstanceAddedEventListener}s.
     */
    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    /**
     * Returns the space instance removed event manager allowing to add and remove
     * {@link org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener}s.
     */
    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    /**
     * Allows to add a {@link org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener}.
     */
    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link SpaceInstanceLifecycleEventListener}.
     */
    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);
}
