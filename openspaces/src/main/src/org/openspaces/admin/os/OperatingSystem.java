package org.openspaces.admin.os;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventManager;

/**
 * @author kimchy
 */
public interface OperatingSystem extends AdminAware, StatisticsMonitor {

    String getUid();

    OperatingSystemDetails getDetails();

    OperatingSystemStatistics getStatistics();

    OperatingSystemStatisticsChangedEventManager getStatisticsChanged();
}
