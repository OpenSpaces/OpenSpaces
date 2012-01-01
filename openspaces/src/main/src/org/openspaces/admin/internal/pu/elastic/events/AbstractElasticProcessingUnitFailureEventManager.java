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
