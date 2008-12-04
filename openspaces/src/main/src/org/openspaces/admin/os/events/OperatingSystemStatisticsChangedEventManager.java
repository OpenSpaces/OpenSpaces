package org.openspaces.admin.os.events;

/**
 * @author kimchy
 */
public interface OperatingSystemStatisticsChangedEventManager {

    void add(OperatingSystemStatisticsChangedEventListener eventListener);

    void remove(OperatingSystemStatisticsChangedEventListener eventListener);
}