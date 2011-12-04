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

    //keep a cursor per-listener with the last provision event sequence id.
    private final Map<L, EventCursor> eventListeners = new ConcurrentHashMap<L, EventCursor>();

    private final class EventCursor {
        //Maps partition-Id/pu-name -> ProvisionLifeCycleEvent
        private final Map<String, ProvisionLifeCycleEvent> lastProvisionEvents = new HashMap<String, ProvisionLifeCycleEvent>();
    }
    
    public AbstractProcessingUnitInstanceProvisionEventManager(InternalAdmin admin) {
        this.admin = admin;
    }
    
    public void add(L listener) {
        eventListeners.put(listener, new EventCursor());
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
    protected List<L> filterListenersBySequenceId(ProvisionLifeCycleEvent provisionEvent) {
        List<L> list = new ArrayList<L>();
        for (Entry<L,EventCursor> entry : eventListeners.entrySet()) {
            L listener = entry.getKey();
            EventCursor eventCursor = entry.getValue();
            
            String partitionId = provisionEvent.getPartitionId() != null ? provisionEvent.getPartitionId() : provisionEvent.getProcessingUnitName();
            ProvisionLifeCycleEvent lastProvisionEvent = eventCursor.lastProvisionEvents.get(partitionId);
            if (lastProvisionEvent != null && !isNewEvent(provisionEvent, lastProvisionEvent)) {
                continue;
            }
            
            //update with last ProvisionLifeCycleEvent for this listener
            eventCursor.lastProvisionEvents.put(partitionId, provisionEvent);
            list.add(listener);
        }
        
        return list;
    }

    /*
     * Determine if this is a new event by comparing the source GSM service ID and the sequence ID applied by this GSM.
     */
    private boolean isNewEvent(ProvisionLifeCycleEvent provisionEvent, ProvisionLifeCycleEvent lastProvisionEvent) {
        if (provisionEvent == lastProvisionEvent) {
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
