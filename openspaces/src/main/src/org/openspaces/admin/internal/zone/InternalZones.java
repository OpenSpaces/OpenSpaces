package org.openspaces.admin.internal.zone;

import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.Zones;

/**
 * @author kimchy
 */
public interface InternalZones extends Zones {

    void addZone(InternalZone zone, String zoneUidProvider);

    void removeProvider(Zone zone, String zoneUidProvider);
}
