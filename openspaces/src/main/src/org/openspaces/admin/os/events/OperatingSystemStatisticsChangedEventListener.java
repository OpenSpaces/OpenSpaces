package org.openspaces.admin.os.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface OperatingSystemStatisticsChangedEventListener extends AdminEventListener {

    void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event);
}