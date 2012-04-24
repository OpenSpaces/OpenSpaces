package org.openspaces.admin.pu.statistics;

import java.util.Map;

import org.openspaces.core.util.StringProperties;


/**
 * Identifies a processing unit statistics value, by specifying the monitoring source and the statistics functions applied to it.
 * @author itaif
 * @see ProcessingUnitStatisticsIdConfigurer
 * @since 9.0.0
 */
public class ProcessingUnitStatisticsId {

    private static final String MONITOR_KEY = "monitor";

    private static final String METRIC_KEY = "metric";

    private static final String TIMEWINDOW_STATISTICS_KEY = "timewindow-statistics";
    private static final String INSTANCES_STATISTICS_KEY = "instances-statistics";

    StringProperties properties;
    
    /** 
     * default constructor
     */
    public ProcessingUnitStatisticsId() {
        this.properties = new StringProperties();
    }
    
    public ProcessingUnitStatisticsId(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public String getMonitor() {
        return properties.get(MONITOR_KEY);
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#monitor(String) 
     */
    public void setMonitor(String monitor) {
        this.properties.put(MONITOR_KEY, monitor);
    }
    
    public String getMetric() {
        return properties.get(METRIC_KEY);
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#metric(String)
     */
    public void setMetric(String metric) {
        this.properties.put(METRIC_KEY, metric);
    }
    
    public TimeWindowStatisticsConfig getTimeWindowStatistics() {
        return (TimeWindowStatisticsConfig) properties.getMapWrapperObject(TIMEWINDOW_STATISTICS_KEY, null);
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#timeWindowStatistics(TimeWindowStatisticsConfig)
     */
    public void setTimeWindowStatistics(TimeWindowStatisticsConfig timeWindowStatistics) {
        properties.putMapWrapperObject(TIMEWINDOW_STATISTICS_KEY, timeWindowStatistics.getProperties(), timeWindowStatistics.getClass());
    }

    public InstancesStatisticsConfig getInstancesStatistics() {
        return (InstancesStatisticsConfig) properties.getMapWrapperObject(INSTANCES_STATISTICS_KEY, null);
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#instancesStatistics(InstancesStatisticsConfig)
     */
    public void setInstancesStatistics(InstancesStatisticsConfig instancesStatistics) {
        properties.putMapWrapperObject(INSTANCES_STATISTICS_KEY, instancesStatistics.getProperties(), instancesStatistics.getClass());
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        ProcessingUnitStatisticsId other = (ProcessingUnitStatisticsId) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "processingUnitStatisticsId " + properties.toString();
    }

    /**
     * Checks that the content of this StatisticsId is valid.
     * @throws IllegalStateException - if state is found to be illegal
     */
    public void validate() throws IllegalStateException {
        if (getTimeWindowStatistics() == null) {
            throw new IllegalStateException("timeWindowStatistics cannot be null");
        }
        getTimeWindowStatistics().validate();
        
        InstancesStatisticsConfig instancesStatistics = (InstancesStatisticsConfig)getInstancesStatistics();
        if (instancesStatistics == null) {
            throw new IllegalStateException("instancesStatistics cannot be null");
        }
        instancesStatistics.validate();
        
        if (getMetric() == null || getMonitor().isEmpty()) {
            throw new IllegalStateException("metric name cannot be null or empty");
        }
        
        if (getMonitor() == null || getMonitor().isEmpty()) {
            throw new IllegalStateException("monitor name cannot be null or empty");
        }
    }

    public Map<String, String> getProperties() {
        return properties.getProperties();
    }

}
