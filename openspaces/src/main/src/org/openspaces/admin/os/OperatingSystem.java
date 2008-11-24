package org.openspaces.admin.os;

import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;

/**
 * @author kimchy
 */
public interface OperatingSystem {

    String getUID();

    OperatingSystemConfiguration getConfiguration();

    OperatingSystemStatistics getStatistics();
}
