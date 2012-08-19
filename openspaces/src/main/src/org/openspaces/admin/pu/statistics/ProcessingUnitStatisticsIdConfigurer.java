package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.zone.config.ZonesConfig;


/**
 * Provides fluent API for creating a new {@link ProcessingUnitStatisticsId} object.
 * @author itaif
 * @since 9.0.0
 * @see ProcessingUnitStatisticsId
 */
public class ProcessingUnitStatisticsIdConfigurer {
        
    private final ProcessingUnitStatisticsId config;

    public ProcessingUnitStatisticsIdConfigurer() {
        config = new ProcessingUnitStatisticsId();
    }
    
    /**
     * The processing unit custom service monitors id that is the source for this statistics 
     * For more info see {@link org.openspaces.pu.service.ServiceMonitorsProvider} and {@link org.openspaces.pu.service.ServiceMonitors#getId()}
     * 
     * Could also be one of the following admin API reserved values:
     * "org.openspaces.admin.os.OperatingSystemsStatistics" for Operating System statistics
     * "org.openspaces.admin.vm.VirtualMachineStatistics" for the Java Virtual Machine statistics
     * 
     */
    public ProcessingUnitStatisticsIdConfigurer monitor(String monitor) {
        config.setMonitor(monitor);
        return this;
    }
    
    /**
     * The name of the statistics in the source.
     */
    public ProcessingUnitStatisticsIdConfigurer metric(String metric) { 
        config.setMetric(metric);
        return this;
    }
    
    /**
     * Aggregates values using the specified time window statistics function.
     * If timeWindow is null selects the last sample.
     */
    public ProcessingUnitStatisticsIdConfigurer timeWindowStatistics(TimeWindowStatisticsConfig timeWindowStatistics) {
        config.setTimeWindowStatistics(timeWindowStatistics);
        return this;
    }

    /**
     * Aggregates values of using the specified cluster statistics function
     */
    public ProcessingUnitStatisticsIdConfigurer instancesStatistics(InstancesStatisticsConfig instancesStatistics) {
        config.setInstancesStatistics(instancesStatistics);
        return this;
    }
    
    public ProcessingUnitStatisticsIdConfigurer zoneStatistics(ZonesConfig zoneStatistics) {
        config.setZoneStatistics(zoneStatistics);
        return this;
    }
    
    
    public ProcessingUnitStatisticsId create() {
        return config;
    }
    
}
