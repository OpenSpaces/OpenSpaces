package org.openspaces.admin.internal.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;

import com.gigaspaces.internal.jvm.JVMStatistics;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineStatistics implements VirtualMachineStatistics {

    private static final JVMStatistics NA_STATS = new JVMStatistics();

    private final long timeDelta;

    private final JVMStatistics stats;

    private final VirtualMachineDetails details;

    private volatile VirtualMachineStatistics previousStats;

    private final double cpuPerc;
    
    private final double gcCollectionPerc;
    
    private final long previousTimeStamp;
    
    public DefaultVirtualMachineStatistics() {
        this(NA_STATS, null, null, 0, -1);
    }

    public DefaultVirtualMachineStatistics(JVMStatistics stats, VirtualMachineStatistics previousStats, VirtualMachineDetails details, int historySize, long timeDelta) {
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;
        this.timeDelta = timeDelta;

        if (previousStats == null) {
            this.cpuPerc = -1;
            this.gcCollectionPerc = -1;
            this.previousTimeStamp = -1;
        } else { 
            this.cpuPerc = stats.computeCpuPerc(((DefaultVirtualMachineStatistics)previousStats).stats);
            this.previousTimeStamp = previousStats.getTimestamp();
            this.gcCollectionPerc = StatisticsUtils.computePercByTime(getGcCollectionTime(), previousStats.getGcCollectionTime(), getTimestamp(), previousTimeStamp);
        }
        
        VirtualMachineStatistics lastStats = previousStats;
        
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultVirtualMachineStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return stats.isNA();
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

    public VirtualMachineDetails getDetails() {
        return this.details;
    }

    public List<VirtualMachineStatistics> getTimeline() {
        ArrayList<VirtualMachineStatistics> timeline = new ArrayList<VirtualMachineStatistics>();
        timeline.add(this);
        VirtualMachineStatistics current = this.getPrevious();
        while (current != null && !current.isNA()) {
            timeline.add(current);
            current = current.getPrevious();
        }
        return timeline;
    }

    public VirtualMachineStatistics getPrevious() {
        return previousStats;
    }

    public void setPreviousStats(VirtualMachineStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public long getPreviousTimestamp() {
        return previousTimeStamp;
    }

    public long getUptime() {
        return stats.getUptime();
    }

    public long getMemoryHeapCommittedInBytes() {
        return stats.getMemoryHeapCommitted();
    }

    public double getMemoryHeapCommittedInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapCommittedInBytes());
    }

    public double getMemoryHeapCommittedInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapCommittedInBytes());
    }

    public long getMemoryHeapUsedInBytes() {
        return stats.getMemoryHeapUsed();
    }

    public double getMemoryHeapUsedInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapUsedInBytes());
    }

    public double getMemoryHeapUsedInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapUsedInBytes());
    }

    public double getMemoryHeapUsedPerc() {
        if (isNA()) {
            return -1;
        } else {
            return StatisticsUtils.computePerc(getMemoryHeapUsedInBytes(), getDetails().getMemoryHeapMaxInBytes());
        }
    }

    public double getMemoryHeapCommittedUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryHeapUsedInBytes(), getMemoryHeapCommittedInBytes());
    }

    public long getMemoryNonHeapCommittedInBytes() {
        return stats.getMemoryNonHeapCommitted();
    }

    public double getMemoryNonHeapCommittedInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapCommittedInBytes());
    }

    public double getMemoryNonHeapCommittedInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapCommittedInBytes());
    }

    public long getMemoryNonHeapUsedInBytes() {
        return stats.getMemoryNonHeapUsed();
    }

    public double getMemoryNonHeapUsedInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapUsedInBytes());
    }

    public double getMemoryNonHeapUsedInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapUsedInBytes());
    }

    public double getMemoryNonHeapUsedPerc() {
        if (isNA()) {
            return -1;
        } else {
            return StatisticsUtils.computePerc(getMemoryNonHeapUsedInBytes(), getDetails().getMemoryNonHeapMaxInBytes());
        }
    }

    public double getMemoryNonHeapCommittedUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryNonHeapUsedInBytes(), getMemoryNonHeapCommittedInBytes());
    }

    public int getThreadCount() {
        return stats.getThreadCount();
    }

    public int getPeakThreadCount() {
        return stats.getPeakThreadCount();
    }

    public long getGcCollectionCount() {
        return stats.getGcCollectionCount();
    }

    public long getGcCollectionTime() {
        return stats.getGcCollectionTime();
    }

    public double getGcCollectionPerc() {
       return gcCollectionPerc;
    }

    public double getCpuPerc() {
        return cpuPerc;
    }

    public double getCpuPercAverage(long requestedTotalTime, TimeUnit timeUnit) {
        if (requestedTotalTime <= 0) {
            throw new IllegalArgumentException("Total time has to be positive");
        }
        
        long requestedTotalTimeMillis = TimeUnit.MILLISECONDS.convert(requestedTotalTime, timeUnit);

        long endTimeStamp = stats.getTimestamp();
        
        DefaultVirtualMachineStatistics start = (DefaultVirtualMachineStatistics)getPrevious();
        DefaultVirtualMachineStatistics previousStart = null;

        long duration = endTimeStamp - start.getTimestamp();
        long previousDuration = 0;
        while (duration < requestedTotalTimeMillis) {
            previousStart = start;
            previousDuration = duration;
            
            start = (DefaultVirtualMachineStatistics)previousStart.getPrevious();
            
            if (start == null) {
                return -1;
            }
            
            duration = endTimeStamp - start.getTimestamp();
        }
        
        if (duration == previousDuration) {
            throw new IllegalStateException("This can never happen");
        } 
        
        double result;
        
        if (previousDuration == 0) {
            result = stats.computeCpuPerc(start.stats);
        } else {
            // weighted average of duration and previousDuration to get a precise average based on the requestedTotalTime
            long timeSlot = duration - previousDuration;
            double rightWeight = (double)(requestedTotalTimeMillis - previousDuration) / timeSlot;
            double leftWeight = (double)(duration - requestedTotalTimeMillis) / timeSlot;
            
            result = rightWeight * stats.computeCpuPerc(start.stats) +
                     leftWeight  * stats.computeCpuPerc(previousStart.stats);
        }

        return result;
    }
    
    public String getCpuPercFormatted() {
        return StatisticsUtils.formatPerc(getCpuPerc());
    }
}
