package org.openspaces.admin.internal.zone;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.zone.events.DefaultZoneAddedEventManager;
import org.openspaces.admin.internal.zone.events.DefaultZoneRemovedEventManager;
import org.openspaces.admin.internal.zone.events.InternalZoneAddedEventManager;
import org.openspaces.admin.internal.zone.events.InternalZoneRemovedEventManager;
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.events.ZoneAddedEventListener;
import org.openspaces.admin.zone.events.ZoneAddedEventManager;
import org.openspaces.admin.zone.events.ZoneLifecycleEventListener;
import org.openspaces.admin.zone.events.ZoneRemovedEventManager;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultZones implements InternalZones {

    private final InternalAdmin admin;

    private final Map<String, Zone> zonesByName = new SizeConcurrentHashMap<String, Zone>();

    private final Map<String, Set<String>> zonesProviders = new HashMap<String, Set<String>>();

    private final InternalZoneAddedEventManager zoneAddedEventManager;

    private final InternalZoneRemovedEventManager zoneRemovedEventManager;


    public DefaultZones(InternalAdmin admin) {
        this.admin = admin;
        this.zoneAddedEventManager = new DefaultZoneAddedEventManager(this);
        this.zoneRemovedEventManager = new DefaultZoneRemovedEventManager(this);
    }
    
    public Zone[] getZones() {
        return zonesByName.values().toArray(new Zone[0]);
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

    public ZoneAddedEventManager getZoneAdded() {
        return zoneAddedEventManager;
    }

    public ZoneRemovedEventManager getZoneRemoved() {
        return zoneRemovedEventManager;
    }

    public void addLifecycleListener(ZoneLifecycleEventListener eventListener) {
        getZoneAdded().add(eventListener);
        getZoneRemoved().add(eventListener);
    }

    public void removeLifeycleListener(ZoneLifecycleEventListener eventListener) {
        getZoneAdded().remove(eventListener);
        getZoneRemoved().remove(eventListener);
    }

    public void addZone(InternalZone zone, String zoneUidProvider) {
        Set<String> providers = zonesProviders.get(zone.getName());
        if (providers == null) {
            providers = new HashSet<String>();
            zonesProviders.put(zone.getName(), providers);
        }
        providers.add(zoneUidProvider);
        Zone existing = zonesByName.put(zone.getName(), zone);
        if (existing == null) {
            zoneAddedEventManager.zoneAdded(zone);
        }
    }

    public void removeProvider(Zone zone, String zoneUidProvider) {
        Set<String> providers = zonesProviders.get(zone.getName());
        if (providers != null) {
            providers.remove(zoneUidProvider);
            if (providers.isEmpty()) {
                // note, currently we do not remove the providers from the zonesProviders
                // this allows us to track the zones we had (if we want to expose that)
                Zone existingZone = zonesByName.remove(zone.getName());
                if (existingZone != null) {
                    zoneRemovedEventManager.zoneRemoved(existingZone);
                }
            }
        }
    }
    
    public Zone waitFor(String zoneByName) {
        return waitFor(zoneByName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }
    
    public Zone waitFor(final String zoneByName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Zone> ref = new AtomicReference<Zone>();
        ZoneAddedEventListener added = new ZoneAddedEventListener() {
            
            public void zoneAdded(Zone zone) {
                if (zone.getName().equals(zoneByName)) {
                    ref.set(zone);
                    latch.countDown();
                }
                
            }
        };
        getZoneAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getZoneAdded().remove(added);
        }
    }
}
