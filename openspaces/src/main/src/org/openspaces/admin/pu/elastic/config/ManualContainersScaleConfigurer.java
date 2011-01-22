package org.openspaces.admin.pu.elastic.config;

/**
 * Provides fluent API for creating a new {@link ManualContainersScaleConfig} object.
 * 
 * For example {@code new ManualContainersScaleStrategyConfigurer().numberOfContainers(10).getConfig()}
 * 
 * @author itaif
 * 
 */
public class ManualContainersScaleConfigurer implements ScaleStrategyConfigurer<ManualContainersScaleConfig>{

    private final ManualContainersScaleConfig config;

    /**
     * Provides fluent API for creating a new {@link ManualContainersScaleConfig} object.
     * For example {@code new ManualContainersScaleStrategyConfigurer().numberOfContainers(10).getConfig()}
     * The default constructor wraps an empty {@link ManualContainersScaleConfig} object
     */
    public ManualContainersScaleConfigurer() {
        this.config = new ManualContainersScaleConfig();
    }
    
    /**
     * @see ManualContainersScaleConfig#setNumberOfContainers(int)
     */
    public ManualContainersScaleConfigurer numberOfContainers(int numberOfContainers) {
        config.setNumberOfContainers(numberOfContainers);
        return this;
    }

    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public ManualContainersScaleConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public ManualContainersScaleConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        config.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see ScaleStrategyConfigurer#getConfig()
     */
    public ManualContainersScaleConfig getConfig() {
        return config;
    }
}
