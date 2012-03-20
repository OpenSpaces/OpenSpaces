package org.openspaces.admin.pu.statistics;


public class AverageTimeWindowStatisticsConfig extends AbstractTimeWindowStatisticsConfig {

    @Override
    public String toString() {
        return "averageTimeWindowStatistics {timeWindowSeconds="+getTimeWindowSeconds() + ", minimumTimeWindowSeconds="+getMinimumTimeWindowSeconds() + ", maximumTimeWindowSeconds="+ getMaximumTimeWindowSeconds()+"}";
    }
}
