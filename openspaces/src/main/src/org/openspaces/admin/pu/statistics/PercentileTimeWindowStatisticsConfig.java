package org.openspaces.admin.pu.statistics;


public class PercentileTimeWindowStatisticsConfig extends AbstractTimeWindowStatisticsConfig {

    private double percentile;
        
    public double getPercentile() {
        return percentile;
    }
    
    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(percentile);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PercentileTimeWindowStatisticsConfig other = (PercentileTimeWindowStatisticsConfig) obj;
        if (Double.doubleToLongBits(percentile) != Double.doubleToLongBits(other.percentile))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "percentile { percentile="+percentile+" , timeWindowSeconds="+getTimeWindowSeconds() + ", minimumTimeWindowSeconds="+getMinimumTimeWindowSeconds() + ", maximumTimeWindowSeconds="+ getMaximumTimeWindowSeconds()+"}";
    }    
}
