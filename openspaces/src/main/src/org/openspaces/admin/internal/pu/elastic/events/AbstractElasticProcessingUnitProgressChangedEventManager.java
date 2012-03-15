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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitProgressChangedEvent;

public abstract class AbstractElasticProcessingUnitProgressChangedEventManager<TE extends ElasticProcessingUnitProgressChangedEvent ,TEL> {

    final List<TE> lastProgressEvents = new ArrayList<TE>();
    
    private final List<TEL> listeners = new ArrayList<TEL>();
    
    private final InternalAdmin admin;
    
    public AbstractElasticProcessingUnitProgressChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }
    
    /**
     * Add the specified listener as a subscriber for events.
     * If specified the last progress events (one per processing unit) are invoked.
     */
    public void add(final TEL listener, boolean includeLastProgressEvent) {
        
        synchronized (lastProgressEvents) {
            if (includeLastProgressEvent) {
    
                final List<TE> lastProgressEventsSnapshot = 
                        new ArrayList<TE>(lastProgressEvents);
                
                //push events to the listeners
                admin.raiseEvent(listener, new Runnable() {
                    public void run() {
                        for (TE event : lastProgressEventsSnapshot) {
                            AbstractElasticProcessingUnitProgressChangedEventManager.this.fireEventToListener(event,listener);
                        }
                    }
                });
            }
            listeners.add(listener);
        }
    }

    /**
     * Add the specified listener as a subscriber for events.
     * The last progress events (one per processing unit) are invoked.
     */    
    public void add(TEL listener) {
       add(listener, true);
    }

    /**
     * Remove the specified listener as a subscriber for events.
     */ 
    public void remove(TEL listener) {
        synchronized (lastProgressEvents) {
            listeners.remove(listener);
        }
    }

    /**
     * push the specified event to all listeners 
     */
    protected void pushEventToAllListeners(final TE event) {
        
        synchronized (lastProgressEvents) {

            putProgressEvent(event);
            
            for (final TEL listener : listeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        AbstractElasticProcessingUnitProgressChangedEventManager.this.fireEventToListener(event,listener);
                    }
                });
            }
        }
    }
    
    /**
     * Invoke the strongly typed listener method on the specified listener with the specified event
     */
    protected abstract void fireEventToListener(TE event, TEL listener);

    private void putProgressEvent(TE newEvent) {

        //remove any existing events with the same pu name
        Iterator<TE> iterator = lastProgressEvents.iterator();
        while (iterator.hasNext()) {
            TE event = iterator.next();
            if (event.getProcessingUnitName().equals(newEvent.getProcessingUnitName())) {
                iterator.remove();
            }
        }
        
        //add event to the end of the list
        lastProgressEvents.add(newEvent);
    }

    
}
