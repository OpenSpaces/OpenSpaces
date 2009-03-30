package org.openspaces.admin.internal.zone.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.events.ZoneAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureZoneAddedEventListener extends AbstractClosureEventListener implements ZoneAddedEventListener {

    public ClosureZoneAddedEventListener(Object closure) {
        super(closure);
    }

    public void zoneAdded(Zone zone) {
        getClosure().call(zone);
    }
}