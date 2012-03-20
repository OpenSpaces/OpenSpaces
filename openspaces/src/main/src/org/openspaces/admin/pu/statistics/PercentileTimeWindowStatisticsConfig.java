package org.openspaces.admin.pu.statistics;

public class PercentileTimeWindowStatisticsConfig extends TimeWindowStatisticsConfig {

    private int percentile;
        
    public int getPercentile() {
        return percentile;
    }
    
    public void setPercentile(int percentile) {
        this.percentile = percentile;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + percentile;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PercentileTimeWindowStatisticsConfig other = (PercentileTimeWindowStatisticsConfig) obj;
        if (percentile != other.percentile)
            return false;
        return true;
    }

    
}
