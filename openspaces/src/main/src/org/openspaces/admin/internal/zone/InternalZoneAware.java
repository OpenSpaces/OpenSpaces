package org.openspaces.admin.internal.zone;

import org.openspaces.admin.zone.ZoneAware;
import org.openspaces.admin.zone.Zone;

/**
 * @author kimchy
 */
public interface InternalZoneAware extends ZoneAware {

    void addZone(Zone zone);
}
