package org.openspaces.admin.internal.lus;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.lus.events.DefaultLookupServiceAddedEventManager;
import org.openspaces.admin.internal.lus.events.DefaultLookupServiceRemovedEventManager;
import org.openspaces.admin.internal.lus.events.InternalLookupServiceAddedEventManager;
import org.openspaces.admin.internal.lus.events.InternalLookupServiceRemovedEventManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceAddedEventManager;
import org.openspaces.admin.lus.events.LookupServiceLifecycleEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultLookupServices implements InternalLookupServices {

    private final InternalAdmin admin;

    private final Map<String, LookupService> lookupServiceMap = new SizeConcurrentHashMap<String, LookupService>();

    private final InternalLookupServiceAddedEventManager lookupServiceAddedEventManager;

    private final InternalLookupServiceRemovedEventManager lookupServiceRemovedEventManager;

    public DefaultLookupServices(InternalAdmin admin) {
        this.admin = admin;
        this.lookupServiceAddedEventManager = new DefaultLookupServiceAddedEventManager(this);
        this.lookupServiceRemovedEventManager = new DefaultLookupServiceRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
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

    public void addLifecycleListener(LookupServiceLifecycleEventListener eventListener) {
        getLookupServiceAdded().add(eventListener);
        getLookupServiceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(LookupServiceLifecycleEventListener eventListener) {
        getLookupServiceAdded().remove(eventListener);
        getLookupServiceRemoved().remove(eventListener);
    }

    public void addLookupService(final InternalLookupService lookupService) {
        LookupService existingLookupService = lookupServiceMap.put(lookupService.getUid(), lookupService);
        if (existingLookupService == null) {
            lookupServiceAddedEventManager.lookupServiceAdded(lookupService);
        }
    }

    public InternalLookupService removeLookupService(String UID) {
        final InternalLookupService existingLookupService = (InternalLookupService) lookupServiceMap.remove(UID);
        if (existingLookupService != null) {
            lookupServiceRemovedEventManager.lookupServiceRemoved(existingLookupService);
        }
        return existingLookupService;
    }

    public LookupServiceAddedEventManager getLookupServiceAdded() {
        return this.lookupServiceAddedEventManager;
    }

    public LookupServiceRemovedEventManager getLookupServiceRemoved() {
        return this.lookupServiceRemovedEventManager;
    }
}