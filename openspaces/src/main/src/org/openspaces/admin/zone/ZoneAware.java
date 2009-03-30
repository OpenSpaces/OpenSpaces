package org.openspaces.admin.zone;

import java.util.Map;

/**
 * @author kimchy
 */
public interface ZoneAware {

    Map<String, Zone> getZones();
}
