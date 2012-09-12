/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import java.util.Iterator;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstances extends InternalProcessingUnitInstancesAware {

    boolean contains(ProcessingUnitInstance processingUnitInstance);

    void addOrphaned(ProcessingUnitInstance processingUnitInstance);

    ProcessingUnitInstance removeOrphaned(String uid);

    void addInstance(ProcessingUnitInstance processingUnitInstance);

    ProcessingUnitInstance removeInstance(String uid);

    ProcessingUnitInstance[] getOrphaned();

    Iterator<ProcessingUnitInstance> getInstancesIt();

    ProcessingUnitInstance[] getInstances();

    ProcessingUnitInstance[] getInstances(String processingUnitName);

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    ProcessingUnitInstance getInstanceByUID(String processingUnitInstanceUid);
}
