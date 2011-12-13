package org.openspaces.admin.internal.pu.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;

public class AbstractProcessingUnitInstanceProvisionEventManager<L extends AdminEventListener> {

    protected final InternalAdmin admin;

    //keep a mapping per-listener with the last provision event sequence id indexed by processing unit instance name.
    //Maps Listener -> Map of processing unit instance name -> ProvisionLifeCycleEvent
    private final Map<L, Map<String, ProvisionLifeCycleEvent>> eventListeners = new ConcurrentHashMap<L, Map<String, ProvisionLifeCycleEvent>>();

    public AbstractProcessingUnitInstanceProvisionEventManager(InternalAdmin admin) {
        this.admin = admin;
    }
    
    public void add(L listener) {
        eventListeners.put(listener, new HashMap<String, ProvisionLifeCycleEvent>());
    }

    public void remove(L listener) {
        eventListeners.remove(listener);
    }
    
    /*
     * Extract only the listeners which haven't yet received this event using the event sequenceId. Listeners are updated with last sequence id.
     * 
     * For each listener we keep a map of <partition-id,ProvisionLifeCycleEvent> where the partition-id is either the [pu-name.instanceId] or just the [pu-name].
     * The reason we keep this map is because the ProvisionLifeCycleEvent.sequenceId we receive from the GSM is per partition (ServiceElement).
     */
    protected List<L> filterListenersBySequenceId(ProvisionLifeCycleEvent provisionEvent, DefaultProcessingUnitInstanceProvisionEventsManager processingUnitInstanceProvisionEventsManager) {
        List<L> matchingListeners = new ArrayList<L>();
        for (Entry<L,Map<String, ProvisionLifeCycleEvent>> entry : eventListeners.entrySet()) {
            L listener = entry.getKey();
            Map<String, ProvisionLifeCycleEvent> indexEventsByProcessingUnitInstanceName = entry.getValue();
            
            String key = provisionEvent.getProcessingUnitInstanceName();
            ProvisionLifeCycleEvent lastProvisionEvent = indexEventsByProcessingUnitInstanceName.get(key);
            
            if (!isNewEvent(provisionEvent, lastProvisionEvent)) {
                continue; //not a new event for this listener
            }
            
            //update with last ProvisionLifeCycleEvent for this listener
            indexEventsByProcessingUnitInstanceName.put(key, provisionEvent);

            if (!processingUnitInstanceProvisionEventsManager.isListenerAssociatedWithProcessingUnitInstance(listener, key)) {
                if (!processingUnitInstanceProvisionEventsManager.isLastProvisionEvent(provisionEvent)) {
                    continue;  //not the last event for this processing unit instance - we need to reflect state.
                }
            }
            
            matchingListeners.add(listener);
            processingUnitInstanceProvisionEventsManager.associateProcessingUnitInstanceNameWithListener(listener, key);
        }
        
        return matchingListeners;
    }

    /*
     * Determine if this is a new event by comparing the source GSM service ID and the sequence ID applied by this GSM.
     */
    private boolean isNewEvent(ProvisionLifeCycleEvent provisionEvent, ProvisionLifeCycleEvent lastProvisionEvent) {
        if (lastProvisionEvent == null) {
            return true;
        }
        
        //same managing GSM but smaller sequence id, not a new event
        if (provisionEvent.getGsmServiceId().equals(lastProvisionEvent.getGsmServiceId()) 
                && provisionEvent.getSequenceId() <= lastProvisionEvent.getSequenceId()) {
            return false;
        }
        //either bigger sequence id or a managing gsm has changed - thus sequence id's don't match anymore.
        return true;
    }
}
