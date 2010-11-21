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

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import java.util.concurrent.TimeUnit;

/**
 * A Grid Service Container is a container for {@link org.openspaces.admin.pu.ProcessingUnitInstance}s
 * allocated to it through the {@link org.openspaces.admin.gsm.GridServiceManager} that manages it.
 *
 * @author kimchy
 */
public interface GridServiceContainer extends AgentGridComponent, Iterable<ProcessingUnitInstance>, LogProviderGridComponent, DumpProvider {

    /**
     * Waits indefinitely for the given number of processing unit instances to run within the container.
     */
    boolean waitFor(int numberOfProcessingUnitInstances);

    /**
     * Waits for timeout value (in time unit) for the given number of processing unit instances to run within the container.
     * Returns <code>true</code> if the number was reached, <code>false</code> if the timeout expired.
     */
    boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit);

    /**
     * Waits indefinitely for the given number of processing unit instances of the specified name to run within the container.
     */
    boolean waitFor(String processingUnitName, int numberOfProcessingUnitInstances);

    /**
     * Waits for timeout value (in time unit) for the given number of processing unit instances of the specified name
     * to run within the container. Returns <code>true</code> if the number was reached, <code>false</code> if the
     * timeout expired.
     */
    boolean waitFor(String processingUnitName, int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit);

    /**
     * Returns the processing unit instances that are currently deployed within the grid service container.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances();

    /**
     * Returns the processing unit instances of the specified name that are currently deployed within the grid service
     * container.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances(String processingUnitName);
    
    /**
     * Returns the processing unit instance that are currently deployed within the grid service container
     * according to its uid.
     * @since 8.0
     */
    ProcessingUnitInstance getProcessingUnitInstanceByUID(String processingUnitInstanceUid);    

    /**
     * Returns <code>true</code> if the GSC contains the provided processing unit instance.
     */
    boolean contains(ProcessingUnitInstance processingUnitInstance);

    /**
     * Returns an event manager allowing to register for processing unit instance additions to the container.
     */
    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    /**
     * Returns an event manager allowing to register for processing unit instance removals from the container.
     */
    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    /**
     * Adds a processing unit lifecycle listener to the container.
     */
    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Removes a processing unit lifecycle listener to the container.
     */
    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);
}