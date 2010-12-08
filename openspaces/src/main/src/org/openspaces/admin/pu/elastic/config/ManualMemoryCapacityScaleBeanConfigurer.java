package org.openspaces.admin.pu.elastic.config;

/**
 * Provides fluent API for creating a new {@link ManualMemoryCapacityScaleBeanConfig} object.
 * 
 * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
 * 
 * @author itaif
 * 
 */
public class ManualMemoryCapacityScaleBeanConfigurer implements ScaleBeanConfigurer<ManualMemoryCapacityScaleBeanConfig>{

private final ManualMemoryCapacityScaleBeanConfig config;
    
    /**
     * default constructor, creates an empty {@link ManualMemoryCapacityScaleBeanConfig} object
     */
    public ManualMemoryCapacityScaleBeanConfigurer() {
        this.config = new ManualMemoryCapacityScaleBeanConfig();
    }
    
    public ManualMemoryCapacityScaleBeanConfigurer capacity(String memory) {
        config.setCapacity(memory);
        return this;
    }
        
    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public ManualMemoryCapacityScaleBeanConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public ManualMemoryCapacityScaleBeanConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        config.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public ManualMemoryCapacityScaleBeanConfigurer maxNumberOfContainers(int maxNumberOfContainers) {
        config.setMaxNumberOfContainers(maxNumberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersScaleConfig#setMinNumberOfContainers(int)
     */
    public ManualMemoryCapacityScaleBeanConfigurer minNumberOfContainers(int minNumberOfContainers) {
        config.setMinNumberOfContainers(minNumberOfContainers);
        return this;
    }
    
    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public ManualMemoryCapacityScaleBeanConfig getConfig() {
        return config;
    }
}
