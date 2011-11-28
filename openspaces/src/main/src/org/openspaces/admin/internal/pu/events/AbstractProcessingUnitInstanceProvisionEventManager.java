package org.openspaces.admin.internal.pu.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;

public class AbstractProcessingUnitInstanceProvisionEventManager<L extends AdminEventListener> {

    protected final InternalAdmin admin;

    //keep a cursor per-listener with the last provision event sequence id.
    private final Map<L, EventCursor> eventListeners = new ConcurrentHashMap<L, EventCursor>();

    private final class EventCursor {
        //Maps partition-Id/pu-name -> sequence-Id
        //usage of AtomicInteger for convenience because it has a set method! 
        //There is no concurrency involved since it is a single threaded call
        private final Map<String, AtomicInteger> lastSequenceIds = new HashMap<String, AtomicInteger>();
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
     * For each listener we keep a map of <partition-id,sequence-id> where the partition-id is either the [pu-name.instanceId] or just the [pu-name].
     * The reason we keep this map is because the sequence id we receive from the GSM is per partition (ServiceElement).
     */
    protected List<L> filterListenersBySequenceId(ProvisionLifeCycleEvent provisionEvent) {
        List<L> list = new ArrayList<L>();
        for (Entry<L,EventCursor> entry : eventListeners.entrySet()) {
            L listener = entry.getKey();
            EventCursor eventCursor = entry.getValue();
            
            String partitionId = provisionEvent.getPartitionId() != null ? provisionEvent.getPartitionId() : provisionEvent.getProcessingUnitName();
            AtomicInteger lastSequenceId = eventCursor.lastSequenceIds.get(partitionId);
            if (lastSequenceId == null) {
                lastSequenceId = new AtomicInteger(-1); 
                eventCursor.lastSequenceIds.put(partitionId, lastSequenceId);
            }
            
            if (provisionEvent.getSequenceId() <= lastSequenceId.get()) {
                continue;
            }
            
            //update with last sequence id for this listener
            lastSequenceId.set(provisionEvent.getSequenceId());
            list.add(listener);
        }
        
        return list;
    }
}
