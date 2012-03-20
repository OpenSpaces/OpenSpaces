package org.openspaces.admin.pu.statistics;

/**
 * Picks the Nth percentile of cluster instance values.
 * For example 50th percentile is the median.
 * @since 9.0.0
 * @author itaif
 *
 */
public class PercentileInstancesStatisticsConfig extends InstancesAggregationStatisticsConfig {

    private int percentile;
    
    public int getPercentile() {
        return percentile;
    }
    
    public void setPercentile(int percentile) {
        this.percentile = percentile;
    }
}
