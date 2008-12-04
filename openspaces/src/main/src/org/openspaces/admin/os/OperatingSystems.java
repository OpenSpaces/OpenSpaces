package org.openspaces.admin.os;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventManager;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface OperatingSystems extends Iterable<OperatingSystem>, AdminAware, StatisticsMonitor {

    OperatingSystem[] getOperatingSystems();

    OperatingSystem getByUID(String uid);

    Map<String, OperatingSystem> getUids();

    int size();

    OperatingSystemsStatistics getStatistics();

    OperatingSystemsStatisticsChangedEventManager getStatisticsChanged();

    OperatingSystemStatisticsChangedEventManager getOperatingSystemStatisticsChanged();
}
