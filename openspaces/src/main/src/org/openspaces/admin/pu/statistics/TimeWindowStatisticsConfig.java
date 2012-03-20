package org.openspaces.admin.pu.statistics;

public abstract class TimeWindowStatisticsConfig {

    private long timeWindowSeconds;

    public long getTimeWindowSeconds() {
        return timeWindowSeconds;
    }
    
    public void setTimeWindowSeconds(long durationSeconds) {
        this.timeWindowSeconds = durationSeconds;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (timeWindowSeconds ^ (timeWindowSeconds >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeWindowStatisticsConfig other = (TimeWindowStatisticsConfig) obj;
        if (timeWindowSeconds != other.timeWindowSeconds)
            return false;
        return true;
    }
}
