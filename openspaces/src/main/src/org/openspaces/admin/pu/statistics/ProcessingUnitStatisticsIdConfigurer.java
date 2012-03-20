package org.openspaces.admin.pu.statistics;


/**
 * 
 * @author itaif
 * @since 9.0.0
 * @see ProcessingUnitStatisticsId
 */
public class ProcessingUnitStatisticsIdConfigurer {
        
    private ProcessingUnitStatisticsId config;

    /**
     * The processing unit custom service monitors id that is the source for this statistics 
     * For more info see {@link org.openspaces.pu.service.ServiceMonitorsProvider} and {@link org.openspaces.pu.service.ServiceMonitors#getId()}
     * 
     * Could also be one of the following admin API reserved values:
     * "org.openspaces.admin.os.OperatingSystemsStatistics" for Operating System statistics
     * "org.openspaces.admin.vm.VirtualMachineStatistics" for the Java Virtual Machine statistics
     * 
     */
    ProcessingUnitStatisticsIdConfigurer monitorId(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * The name of the statistics in the source.
     */
    ProcessingUnitStatisticsIdConfigurer metricId(String name) { 
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Aggregates values using the specified time window statistics function.
     * If timeWindow is null selects the last sample.
     */
    ProcessingUnitStatisticsIdConfigurer timeWindowAggregation(TimeWindowStatisticsConfig timeWindowAggregationStatisticsConfig) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Aggregates values of using the specified cluster statistics function
     * If cluster is null selects each instance statistics individually
     * Cannot be used together with {@link #instanceUid(String)}
     */
    ProcessingUnitStatisticsIdConfigurer instancesAggregation(InstancesStatisticsConfig instancesAggregationStatisticsConfig) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    ProcessingUnitStatisticsId create() {
        return config;
    }
    
}
