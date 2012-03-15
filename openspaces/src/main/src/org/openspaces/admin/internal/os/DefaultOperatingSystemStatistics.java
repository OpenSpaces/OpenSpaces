/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.os;

import com.gigaspaces.internal.os.OSStatistics;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.support.StatisticsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemStatistics implements OperatingSystemStatistics {

    private static final OSStatistics NA_STATS = new OSStatistics();

    private final long timeDelta;

    private final OSStatistics stats;

    private final OperatingSystemDetails details;

    private volatile OperatingSystemStatistics previousStats;

    public DefaultOperatingSystemStatistics() {
        this(NA_STATS, null, null, 0, -1);
    }

    public DefaultOperatingSystemStatistics(OSStatistics stats, OperatingSystemDetails details, OperatingSystemStatistics previousStats,
                                            int historySize, long timeDelta) {
        this.stats = stats;
        this.details = details;
        this.previousStats = previousStats;
        this.timeDelta = timeDelta;
        OperatingSystemStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultOperatingSystemStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public List<OperatingSystemStatistics> getTimeline() {
        ArrayList<OperatingSystemStatistics> timeline = new ArrayList<OperatingSystemStatistics>();
        timeline.add(this);
        OperatingSystemStatistics current = this.getPrevious();
        while (current != null && !current.isNA()) {
            timeline.add(current);
            current = current.getPrevious();
        }
        return timeline;
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public long getAdminTimestamp() {
        if (stats.getTimestamp() != -1 && timeDelta != Integer.MIN_VALUE) {
            return stats.getTimestamp() + timeDelta;
        }
        return -1;
    }

    public OperatingSystemDetails getDetails() {
        return this.details;
    }

    public OperatingSystemStatistics getPrevious() {
        return this.previousStats;
    }

    public void setPreviousStats(OperatingSystemStatistics previosStats) {
        this.previousStats = previosStats;
    }

    public long getFreeSwapSpaceSizeInBytes() {
        return stats.getFreeSwapSpaceSize();
    }

    public double getFreeSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getFreeSwapSpaceSizeInBytes());
    }

    public double getFreeSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getFreeSwapSpaceSizeInBytes());
    }

    public double getSwapSpaceUsedPerc() {
        return StatisticsUtils.computePerc(getDetails().getTotalSwapSpaceSizeInBytes() - getFreeSwapSpaceSizeInBytes(), getDetails().getTotalSwapSpaceSizeInBytes());
    }

    public long getFreePhysicalMemorySizeInBytes() {
        return stats.getFreePhysicalMemorySize();
    }

    public double getFreePhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getFreePhysicalMemorySizeInBytes());
    }

    public double getFreePhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getFreePhysicalMemorySizeInBytes());
    }

    public long getActualFreePhysicalMemorySizeInBytes() {
        return stats.getActualFreePhysicalMemorySize();
    }

    public double getActualFreePhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getActualFreePhysicalMemorySizeInBytes());
    }

    public double getActualFreePhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getActualFreePhysicalMemorySizeInBytes());
    }

    public double getPhysicalMemoryUsedPerc() {
        return StatisticsUtils.computePerc(getDetails().getTotalPhysicalMemorySizeInBytes() - getFreePhysicalMemorySizeInBytes(), getDetails().getTotalPhysicalMemorySizeInBytes());
    }

    public double getActualPhysicalMemoryUsedPerc() {
        return StatisticsUtils.computePerc(getDetails().getTotalPhysicalMemorySizeInBytes() - getActualFreePhysicalMemorySizeInBytes(), getDetails().getTotalPhysicalMemorySizeInBytes());
    }

    public double getCpuPerc() {
        return stats.getCpuPerc();
    }

    public String getCpuPercFormatted() {
        return StatisticsUtils.formatPerc(getCpuPerc());
    }

    private Map<String, NetworkStatistics> netStats;

    public Map<String, NetworkStatistics> getNetworkStats() {
        if (netStats != null) {
            return netStats;
        }
        if (stats.getNetStats() == null) {
            return null;
        }
        Map<String, NetworkStatistics> netStats = new HashMap<String, NetworkStatistics>();
        for (int i = 0; i < stats.getNetStats().length; i++) {
            NetworkStatistics previousNetStats = null;
            if (getPrevious() != null) {
                previousNetStats = getPrevious().getNetworkStats().get(stats.getNetStats()[i].getName());
            }
            netStats.put(stats.getNetStats()[i].getName(), new DefaultNetworkStatistics(stats.getNetStats()[i], previousNetStats));
        }
        this.netStats = netStats;
        return netStats;
    }

    private class DefaultNetworkStatistics implements NetworkStatistics {

        private final OSStatistics.OSNetInterfaceStats netStats;

        private final NetworkStatistics prevNetStats;

        private DefaultNetworkStatistics(OSStatistics.OSNetInterfaceStats netStats, NetworkStatistics prevNetStats) {
            this.netStats = netStats;
            this.prevNetStats = prevNetStats;
        }

        public NetworkStatistics getPrevious() {
            return prevNetStats;
        }

        public String getName() {
            return netStats.getName();
        }

        public long getRxBytes() {
            return netStats.getRxBytes();
        }

        public double getRxBytesPerSecond() {
            if (prevNetStats == null || previousStats == null) {
                return -1;
            }
            return StatisticsUtils.computePerSecond(getRxBytes(), prevNetStats.getRxBytes(), getTimestamp(), previousStats.getTimestamp());
        }

        public long getTxBytes() {
            return netStats.getTxBytes();
        }

        public double getTxBytesPerSecond() {
            if (prevNetStats == null || previousStats == null) {
                return -1;
            }
            return StatisticsUtils.computePerSecond(getTxBytes(), prevNetStats.getTxBytes(), getTimestamp(), previousStats.getTimestamp());
        }

        public long getRxPackets() {
            return netStats.getRxPackets();
        }

        public double getRxPacketsPerSecond() {
            if (prevNetStats == null || previousStats == null) {
                return -1;
            }
            return StatisticsUtils.computePerSecond(getRxPackets(), prevNetStats.getRxPackets(), getTimestamp(), previousStats.getTimestamp());
        }

        public long getTxPackets() {
            return netStats.getTxPackets();
        }

        public double getTxPacketsPerSecond() {
            if (prevNetStats == null || previousStats == null) {
                return -1;
            }
            return StatisticsUtils.computePerSecond(getTxPackets(), prevNetStats.getTxPackets(), getTimestamp(), previousStats.getTimestamp());
        }

        public long getRxErrors() {
            return netStats.getRxErrors();
        }

        public long getTxErrors() {
            return netStats.getTxErrors();
        }

        public long getRxDropped() {
            return netStats.getRxDropped();
        }

        public long getTxDropped() {
            return netStats.getTxDropped();
        }
    }
}
