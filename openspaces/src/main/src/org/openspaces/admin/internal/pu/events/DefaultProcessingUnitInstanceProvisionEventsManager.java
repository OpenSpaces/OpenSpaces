package org.openspaces.admin.internal.pu.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.AdminEventListener;

public class DefaultProcessingUnitInstanceProvisionEventsManager {
    
    private Map<String, List<ProvisionLifeCycleEvent>> eventsIndexedByProcessingUnitInstanceName = new HashMap<String, List<ProvisionLifeCycleEvent>>();
    private Map<AdminEventListener, Set<String>> processingUnitInstanceNameIndexedByListener = new HashMap<AdminEventListener, Set<String>>();
    
    public void indexEventsByProcessingUnitInstanceName(ProvisionLifeCycleEvent[] events) {
        
        Map<String, List<ProvisionLifeCycleEvent>> index = new HashMap<String, List<ProvisionLifeCycleEvent>>();
        
        for (ProvisionLifeCycleEvent event : events) {
            String key = event.getProcessingUnitInstanceName();
            List<ProvisionLifeCycleEvent> list = index.get(key);
            if (list == null) {
                list = new ArrayList<ProvisionLifeCycleEvent>();
                index.put(key, list);
            }
            list.add(event);
        }
        
        eventsIndexedByProcessingUnitInstanceName.clear();
        eventsIndexedByProcessingUnitInstanceName.putAll(index);
    }
    
    public boolean isLastProvisionEvent(ProvisionLifeCycleEvent provisionEvent) {
        List<ProvisionLifeCycleEvent> list = eventsIndexedByProcessingUnitInstanceName.get(provisionEvent.getProcessingUnitInstanceName());
        if (provisionEvent == list.get(list.size() - 1)) {
            return true;
        }
        return false;
    }
    
    public ProvisionLifeCycleEvent getLastProvisionFailureEvent(String processingUnitInstanceName) {
        List<ProvisionLifeCycleEvent> list = eventsIndexedByProcessingUnitInstanceName.get(processingUnitInstanceName);
        ProvisionLifeCycleEvent failureEvent = null;
        for (ProvisionLifeCycleEvent event : list) {
            if (event.getType() == ProvisionLifeCycleEvent.ALLOCATION_FAILURE) {
                failureEvent = event;
            }
        }
        return failureEvent;
    }
    
    public void associateProcessingUnitInstanceNameWithListener(AdminEventListener listener, String processingUnitInstanceName) {
        Set<String> set = processingUnitInstanceNameIndexedByListener.get(listener);
        if (set == null) {
            set = new HashSet<String>();
            processingUnitInstanceNameIndexedByListener.put(listener, set);
        }
        set.add(processingUnitInstanceName);
    }
    
    public boolean isListenerAssociatedWithProcessingUnitInstance(AdminEventListener listener, String processingUnitInstanceName) {
        Set<String> set = processingUnitInstanceNameIndexedByListener.get(listener);
        if (set != null) {
            return set.contains(processingUnitInstanceName);
        }
        return false;
    }
}
