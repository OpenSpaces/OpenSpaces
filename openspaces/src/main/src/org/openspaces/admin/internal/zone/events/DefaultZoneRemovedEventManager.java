package org.openspaces.admin.internal.zone.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.internal.zone.InternalZones;
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.events.ZoneRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultZoneRemovedEventManager implements InternalZoneRemovedEventManager {

    private final InternalZones zones;

    private final InternalAdmin admin;

    private final List<ZoneRemovedEventListener> zoneRemovedEventListeners = new CopyOnWriteArrayList<ZoneRemovedEventListener>();

    public DefaultZoneRemovedEventManager(InternalZones zones) {
        this.zones = zones;
        this.admin = (InternalAdmin) zones.getAdmin();
    }

    public void zoneRemoved(final Zone zone) {
        for (final ZoneRemovedEventListener listener : zoneRemovedEventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.zoneRemoved(zone);
                }
            });
        }
    }

    public void add(ZoneRemovedEventListener eventListener) {
        zoneRemovedEventListeners.add(eventListener);
    }

    public void remove(ZoneRemovedEventListener eventListener) {
        zoneRemovedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureZoneRemovedEventListener(eventListener));
        } else {
            add((ZoneRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureZoneRemovedEventListener(eventListener));
        } else {
            remove((ZoneRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}