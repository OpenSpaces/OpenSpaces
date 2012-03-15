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
package org.openspaces.admin.internal.pu.elastic.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitFailureEvent;

public abstract class AbstractElasticProcessingUnitFailureEventManager<TE extends ElasticProcessingUnitFailureEvent,TEL> {

private final List<TEL> listeners = new CopyOnWriteArrayList<TEL>();
    
    private final InternalAdmin admin;
    
    public AbstractElasticProcessingUnitFailureEventManager(InternalAdmin admin) {
        this.admin = admin;
    }
    
    /**
     * Add the specified listener as a subscriber for events.
     * The last progress events (one per processing unit) are invoked.
     */    
    public void add(TEL listener) {
        listeners.add(listener);
    }

    /**
     * Remove the specified listener as a subscriber for events.
     */ 
    public void remove(TEL listener) {
        listeners.remove(listener);
    }
    
    /**
     * Invoke the strongly typed listener method on the specified listener with the specified event
     */
    protected abstract void fireEventToListener(TE event, TEL listener);
    
    /**
     * push the specified event to all listeners 
     */
    protected void pushEventToAllListeners(final TE event) {
        
        for (final TEL listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    AbstractElasticProcessingUnitFailureEventManager.this.fireEventToListener(event,listener);
                }
            });
        }
    }
}
