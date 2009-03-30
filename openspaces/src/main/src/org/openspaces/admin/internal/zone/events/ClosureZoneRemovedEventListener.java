package org.openspaces.admin.internal.zone.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.events.ZoneRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureZoneRemovedEventListener extends AbstractClosureEventListener implements ZoneRemovedEventListener {

    public ClosureZoneRemovedEventListener(Object closure) {
        super(closure);
    }

    public void zoneRemoved(Zone zone) {
        getClosure().call(zone);
    }
}