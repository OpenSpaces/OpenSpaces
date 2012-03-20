package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.InternalInstancesStatisticsConfig;
import org.openspaces.admin.internal.pu.statistics.InternalTimeWindowStatisticsConfig;


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
     * @see ProcessingUnitStatisticsIdConfigurer#timeWindowStatistics(AbstractTimeWindowStatisticsConfig)
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instancesStatistics == null) ? 0 : instancesStatistics.hashCode());
        result = prime * result + ((metric == null) ? 0 : metric.hashCode());
        result = prime * result + ((monitor == null) ? 0 : monitor.hashCode());
        result = prime * result + ((timeWindowStatistics == null) ? 0 : timeWindowStatistics.hashCode());
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
        if (instancesStatistics == null) {
            if (other.instancesStatistics != null)
                return false;
        } else if (!instancesStatistics.equals(other.instancesStatistics))
            return false;
        if (metric == null) {
            if (other.metric != null)
                return false;
        } else if (!metric.equals(other.metric))
            return false;
        if (monitor == null) {
            if (other.monitor != null)
                return false;
        } else if (!monitor.equals(other.monitor))
            return false;
        if (timeWindowStatistics == null) {
            if (other.timeWindowStatistics != null)
                return false;
        } else if (!timeWindowStatistics.equals(other.timeWindowStatistics))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ProcessingUnitStatisticsId [monitor=" + monitor + ", metric=" + metric + ", timeWindowStatistics="
                + timeWindowStatistics + ", instancesStatistics=" + instancesStatistics + "]";
    }



    /**
     * Checks that the content of this StatisticsId is valid.
     * @throws IllegalStateException - if state is found to be illegal
     */
    public void validate() throws IllegalStateException {
        ((InternalTimeWindowStatisticsConfig)timeWindowStatistics).validate();
        ((InternalInstancesStatisticsConfig)instancesStatistics).validate();
        if (metric == null) {
            throw new IllegalStateException("metric name cannot be null");
        }
        
        if (monitor == null) {
            throw new IllegalStateException("monitor name cannot be null");
        }
    }

}
