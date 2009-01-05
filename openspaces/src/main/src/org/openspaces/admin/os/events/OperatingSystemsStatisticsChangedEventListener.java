package org.openspaces.admin.os.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface OperatingSystemsStatisticsChangedEventListener extends AdminEventListener {

    void operatingSystemsStatisticsChanged(OperatingSystemsStatisticsChangedEvent event);
}