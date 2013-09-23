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
package org.openspaces.admin.internal.pu.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalOrphanProcessingUnitInstancesAware;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author itaif
 * @since 9.7.0
 */
public class DefaultOrphanProcessingUnitInstanceLifecycleEventManager implements InternalOrphanProcessingUnitInstanceLifecycleEventManager {

    private final InternalAdmin admin;

    private final List<InternalOrphanProcessingUnitInstanceLifecycleEventListener> listeners = new CopyOnWriteArrayList<InternalOrphanProcessingUnitInstanceLifecycleEventListener>();

	private final InternalOrphanProcessingUnitInstancesAware instances;

    public DefaultOrphanProcessingUnitInstanceLifecycleEventManager(InternalOrphanProcessingUnitInstancesAware instances, InternalAdmin admin) {
        this.admin = admin;
        this.instances = instances;
    }

    @Override
    public void orphanProcessingUnitInstanceRemoved(final ProcessingUnitInstance processingUnitInstance) {
        for (final InternalOrphanProcessingUnitInstanceLifecycleEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.orphanProcessingUnitInstanceRemoved(processingUnitInstance);
                }
            });
        }
    }


	@Override
	public void orphanProcessingUnitInstanceAdded(final ProcessingUnitInstance processingUnitInstance) {
		
		for (final InternalOrphanProcessingUnitInstanceLifecycleEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.orphanProcessingUnitInstanceAdded(processingUnitInstance);
                }
            });
        }
		
	}
	
	@Override
    public void add(final InternalOrphanProcessingUnitInstanceLifecycleEventListener eventListener) {
		add(eventListener, true);
	}
	
    @Override
    public void add(final InternalOrphanProcessingUnitInstanceLifecycleEventListener eventListener, boolean includeExisting) {
    	if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (ProcessingUnitInstance processingUnitInstance : instances.getOrphanProcessingUnitInstances()) {
                        eventListener.orphanProcessingUnitInstanceAdded(processingUnitInstance);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    @Override
    public void remove(InternalOrphanProcessingUnitInstanceLifecycleEventListener eventListener) {
        listeners.remove(eventListener);
    }
}
