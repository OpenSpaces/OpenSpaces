package org.openspaces.admin.internal.lus;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.LookupServiceEventListener;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultLookupServices implements InternalLookupServices {

    private final InternalAdmin admin;

    private final Map<String, LookupService> lookupServiceMap = new SizeConcurrentHashMap<String, LookupService>();

    private final List<LookupServiceEventListener> eventListeners = new CopyOnWriteArrayList<LookupServiceEventListener>();

    public DefaultLookupServices(InternalAdmin admin) {
        this.admin = admin;
    }

    public LookupService[] getLookupServices() {
        return lookupServiceMap.values().toArray(new InternalLookupService[0]);
    }

    public Iterator<LookupService> iterator() {
        return lookupServiceMap.values().iterator();
    }

    public LookupService getLookupServiceByUID(String id) {
        return lookupServiceMap.get(id);
    }

    public Map<String, LookupService> getUids() {
        return Collections.unmodifiableMap(lookupServiceMap);
    }

    public int size() {
        return lookupServiceMap.size();
    }

    public boolean isEmpty() {
        return lookupServiceMap.isEmpty();
    }

    public void addLookupService(final InternalLookupService lookupService) {
        LookupService existingLookupService = lookupServiceMap.put(lookupService.getUid(), lookupService);
        if (existingLookupService == null) {
            for (final LookupServiceEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.lookupServiceAdded(lookupService);
                    }
                });
            }
        }
    }

    public InternalLookupService removeLookupService(String UID) {
        final InternalLookupService existingLookupService = (InternalLookupService) lookupServiceMap.remove(UID);
        if (existingLookupService != null) {
            for (final LookupServiceEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.lookupServiceRemoved(existingLookupService);
                    }
                });
            }
        }
        return existingLookupService;
    }

    public void addEventListener(final LookupServiceEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (LookupService lookupService : getLookupServices()) {
                    eventListener.lookupServiceAdded(lookupService);
                }
            }
        });
        eventListeners.add(eventListener);
    }

    public void removeEventListener(LookupServiceEventListener eventListener) {
        eventListeners.remove(eventListener);
    }
}