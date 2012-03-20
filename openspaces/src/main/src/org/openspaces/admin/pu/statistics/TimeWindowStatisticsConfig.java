package org.openspaces.admin.pu.statistics;

public abstract class TimeWindowStatisticsConfig {

    private long timeWindowSeconds;

    public long getTimeWindowSeconds() {
        return timeWindowSeconds;
    }
    
    public void setTimeWindowSeconds(long durationSeconds) {
        this.timeWindowSeconds = durationSeconds;
    }
}
