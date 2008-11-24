package org.openspaces.admin.internal.admin.lus;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.LookupService;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultLookupServices implements InternalLookupServices {

    private final Map<String, LookupService> lookupServiceMap = new SizeConcurrentHashMap<String, LookupService>();

    public LookupService[] getLookupServices() {
        return lookupServiceMap.values().toArray(new InternalLookupService[0]);
    }

    public Iterator<LookupService> iterator() {
        return lookupServiceMap.values().iterator();
    }

    public LookupService getLookupServiceByUID(String id) {
        return lookupServiceMap.get(id);
    }

    public int size() {
        return lookupServiceMap.size();
    }

    public void addLookupService(InternalLookupService lookupService) {
        lookupServiceMap.put(lookupService.getUID(), lookupService);
    }

    public InternalLookupService removeLookupService(String UID) {
        return (InternalLookupService) lookupServiceMap.remove(UID);
    }
}