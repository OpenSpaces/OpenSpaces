package org.openspaces.admin.internal.zone;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.Admin;
import com.j_spaces.kernel.SizeConcurrentHashMap;

import java.util.*;

/**
 * @author kimchy
 */
public class DefaultZones implements InternalZones {

    private final InternalAdmin admin;

    private final Map<String, Zone> zonesByName = new SizeConcurrentHashMap<String, Zone>();

    private final Map<String, Set<String>> zonesProviders = new HashMap<String, Set<String>>();

    public DefaultZones(InternalAdmin admin) {
        this.admin = admin;
    }

    public Zone getByName(String name) {
        return zonesByName.get(name);
    }

    public Map<String, Zone> getNames() {
        return Collections.unmodifiableMap(zonesByName);
    }

    public Admin getAdmin() {
        return admin;
    }

    public Iterator<Zone> iterator() {
        return zonesByName.values().iterator();
    }

    public void addZone(InternalZone zone, String zoneUidProvider) {
        Set<String> providers = zonesProviders.get(zone.getName());
        if (providers == null) {
            providers = new HashSet<String>();
            zonesProviders.put(zone.getName(), providers);
        }
        providers.add(zoneUidProvider);
        zonesByName.put(zone.getName(), zone);
    }

    public void removeProvider(Zone zone, String zoneUidProvider) {
        Set<String> providers = zonesProviders.get(zone.getName());
        if (providers != null) {
            providers.remove(zoneUidProvider);
            if (providers.isEmpty()) {
                // note, currently we do not remove the providers from the zonesProviders
                // this allows us to track the zones we had (if we want to expose that)
                Zone existingZone = zonesByName.remove(zone.getName());
            }
        }
    }
}
