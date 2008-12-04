package org.openspaces.admin.os.events;

/**
 * @author kimchy
 */
public interface OperatingSystemsStatisticsChangedEventManager {

    void add(OperatingSystemsStatisticsChangedEventListener eventListener);

    void remove(OperatingSystemsStatisticsChangedEventListener eventListener);
}