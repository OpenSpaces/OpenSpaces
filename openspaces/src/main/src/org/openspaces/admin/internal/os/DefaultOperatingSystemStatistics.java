package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OSStatistics;
import org.openspaces.admin.os.OperatingSystemStatistics;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemStatistics implements OperatingSystemStatistics {

    private static final OSStatistics NA_STATS = new OSStatistics();

    private final OSStatistics stats;

    public DefaultOperatingSystemStatistics() {
        stats = NA_STATS;
    }

    public DefaultOperatingSystemStatistics(OSStatistics stats) {
        this.stats = stats;
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }
}
