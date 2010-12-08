package org.openspaces.admin.pu.elastic.config;

/**
 * Provides fluent API for creating a new {@link ManualContainersScaleBeanConfig} object.
 * 
 * For example {@code new ManualContainersScaleStrategyConfigurer().numberOfContainers(10).getConfig()}
 * 
 * @author itaif
 * 
 */
public class ManualContainersScaleBeanConfigurer implements ScaleBeanConfigurer<ManualContainersScaleBeanConfig>{

    private final ManualContainersScaleBeanConfig config;

    /**
     * default constructor, creates an empty {@link ManualContainersScaleBeanConfig} object
     */
    public ManualContainersScaleBeanConfigurer() {
        this.config = new ManualContainersScaleBeanConfig();
    }
    
    /**
     * @see ManualContainersScaleBeanConfig#setNumberOfContainers(int)
     */
    public ManualContainersScaleBeanConfigurer numberOfContainers(int numberOfContainers) {
        config.setNumberOfContainers(numberOfContainers);
        return this;
    }

    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public ManualContainersScaleBeanConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public ManualContainersScaleBeanConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        config.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public ManualContainersScaleBeanConfig getConfig() {
        return config;
    }
}
