package org.openspaces.admin.os.events;

/**
 * @author kimchy
 */
public interface OperatingSystemsStatisticsChangedEventListener {

    void operatingSystemsStatisticsChanged(OperatingSystemsStatisticsChangedEvent event);
}