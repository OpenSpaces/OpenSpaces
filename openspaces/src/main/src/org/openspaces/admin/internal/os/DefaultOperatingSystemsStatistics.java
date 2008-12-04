package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.OperatingSystemsStatistics;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsStatistics implements OperatingSystemsStatistics {

    private final long timestamp;

    private final OperatingSystemStatistics[] stats;

    public DefaultOperatingSystemsStatistics(OperatingSystemStatistics[] stats) {
        this.timestamp = System.currentTimeMillis();
        this.stats = stats;
    }

    public boolean isNA() {
        return stats == null || stats.length == 0 || stats[0].isNA();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getSize() {
        return stats.length;
    }
}
