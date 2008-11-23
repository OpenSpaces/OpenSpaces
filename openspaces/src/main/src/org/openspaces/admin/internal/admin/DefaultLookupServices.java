package org.openspaces.admin.internal.admin;

import org.openspaces.admin.LookupService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultLookupServices implements InternalLookupServices {

    private final Map<String, InternalLookupService> lookupServiceMap = new ConcurrentHashMap<String, InternalLookupService>();

    public LookupService[] getLookupServices() {
        return lookupServiceMap.values().toArray(new InternalLookupService[0]);
    }

    public LookupService getLookupServiceByUID(String id) {
        return lookupServiceMap.get(id);
    }

    public void addLookupService(InternalLookupService lookupService) {
        lookupServiceMap.put(lookupService.getUID(), lookupService);
    }

    public InternalLookupService removeLookupService(String UID) {
        return lookupServiceMap.remove(UID);
    }
}