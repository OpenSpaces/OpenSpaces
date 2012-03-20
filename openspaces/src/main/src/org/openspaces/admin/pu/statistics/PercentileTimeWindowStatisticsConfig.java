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
    public String toString() {
        return "percentile { percentile="+percentile+" , timeWindowSeconds="+getTimeWindowSeconds() + ", minimumTimeWindowSeconds="+getMinimumTimeWindowSeconds() + ", maximumTimeWindowSeconds="+ getMaximumTimeWindowSeconds()+"}";
    }    
}
