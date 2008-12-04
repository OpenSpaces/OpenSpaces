package org.openspaces.admin.os.events;

/**
 * @author kimchy
 */
public interface OperatingSystemStatisticsChangedEventListener {

    void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event);
}