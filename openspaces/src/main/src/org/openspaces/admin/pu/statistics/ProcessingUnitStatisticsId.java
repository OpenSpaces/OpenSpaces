package org.openspaces.admin.pu.statistics;


/**
 * Identifies a processing unit statistics value, by specifying the monitoring source and the statistics functions applied to it.
 * @author itaif
 * @see ProcessingUnitStatisticsIdConfigurer
 * @since 9.0.0
 */
public class ProcessingUnitStatisticsId {

    private String monitor; 
    
    private String metric;
    
    private TimeWindowStatisticsConfig timeWindowStatistics; 
    
    private InstancesStatisticsConfig instancesStatistics; 
   
    public String getMonitor() {
        return monitor;
    }
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#monitor(String) 
     */
    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }
    
    public String getMetric() {
        return metric;
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#metric(String)
     */
    public void setMetric(String metric) {
        this.metric = metric;
    }
    
    public TimeWindowStatisticsConfig getTimeWindowStatistics() {
        return timeWindowStatistics;
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#timeWindowStatistics(TimeWindowStatisticsConfig)
     */
    public void setTimeWindowStatistics(TimeWindowStatisticsConfig timeWindowStatistics) {
        this.timeWindowStatistics = timeWindowStatistics;
    }

    public InstancesStatisticsConfig getInstancesStatistics() {
        return instancesStatistics;
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#instancesStatistics(InstancesAggregationStatisticsConfig)
     */
    public void setInstancesStatistics(InstancesStatisticsConfig instancesStatistics) {
        this.instancesStatistics = instancesStatistics;
    }       
}
