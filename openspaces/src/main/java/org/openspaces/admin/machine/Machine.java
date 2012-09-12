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

import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.os.OperatingSystem;
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

/**
 * A Machine is a logical entity identified by a host address. Once a grid service is running on a machine
 * ({@link org.openspaces.admin.gsa.GridServiceAgent}, {@link org.openspaces.admin.gsm.GridServiceManager},
 * {@link org.openspaces.admin.gsc.GridServiceContainer}, {@link org.openspaces.admin.lus.LookupService},
 * {@link org.openspaces.admin.space.SpaceInstance}) it is discovered (created) by the admin API.
 *
 * @author kimchy
 */
public interface Machine extends DumpProvider {

    /**
     * Returns the UID of the machine.
     */
    String getUid();

    /**
     * Returns the host address of the machine.
     */
    String getHostAddress();

    /**
     * Returns the host name of the machine.
     */
    String getHostName();

    /**
     * Returns the lookup services that are running on the machine.
     */
    LookupServices getLookupServices();

    /**
     * Returns the first grid service agent. Note, there will usually be only a
     * single agent per machine. Returns <code>null</code> if there is no
     * {@link org.openspaces.admin.gsa.GridServiceAgent} running on the machine.
     */
    GridServiceAgent getGridServiceAgent();

    /**
     * Returns the grid service agents running on the machine.
     */
    GridServiceAgents getGridServiceAgents();

    /**
     * Returns the grid service managers running on the machine.
     */
    GridServiceManagers getGridServiceManagers();
    
    /**
     * Returns the elastic service managers running on the machine.
     */
    ElasticServiceManagers getElasticServiceManagers();

    /**
     * Returns the grid service containers running on the machine.
     */
    GridServiceContainers getGridServiceContainers();

    /**
     * Returns the operating system of the machine.
     */
    OperatingSystem getOperatingSystem();

    /**
     * Returns the virtual machines running on the machine.
     */
    VirtualMachines getVirtualMachines();

    /**
     * Returns <code>true</code> if there are grid components.
     */
    boolean hasGridComponents();

    /**
     * Returns the transports "running" on the machine.
     */
    Transports getTransports();

    /**
     * Returns all the processing unit instances running on the machine.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances();

    /**
     * Returns the processing unit instances of the specified name that are currently deployed on the machine.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances(String processingUnitName);

    /**
     * Returns <code>true</code> if the processing unit instance exists within the machine.
     */
    boolean contains(ProcessingUnitInstance processingUnitInstance);

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
     * Allows to add a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void addLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void removeLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Returns all the space instances running on the machine.
     */
    SpaceInstance[] getSpaceInstances();

    /**
     * Returns <code>true</code> if the machine contains the provided space instance.
     */
    boolean contains(SpaceInstance spaceInstance);

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
     * Allows to add a {@link SpaceInstanceLifecycleEventListener}.
     */
    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link SpaceInstanceLifecycleEventListener}.
     */
    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);
}
