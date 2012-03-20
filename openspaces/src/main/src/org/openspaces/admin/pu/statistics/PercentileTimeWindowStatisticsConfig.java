package org.openspaces.admin.pu.statistics;

public class PercentileTimeWindowStatisticsConfig extends TimeWindowStatisticsConfig {

    private int percentile;
        
    public int getPercentile() {
        return percentile;
    }
    
    public void setPercentile(int percentile) {
        this.percentile = percentile;
    }

}
