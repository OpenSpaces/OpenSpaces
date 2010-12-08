package org.openspaces.admin.pu.elastic.config;

/**
 * Provides fluent API for creating a new {@link EagerScaleBeanConfig} object.
 * 
 * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
 * 
 * @author itaif
 * 
 */
public class EagerScaleBeanConfigurer implements ScaleBeanConfigurer<EagerScaleBeanConfig> {

    private final EagerScaleBeanConfig state;

    /**
     * Create a fluent object that wraps a new {@link EagerScaleBeanConfig} object
     */
    public EagerScaleBeanConfigurer() {
        this.state = new EagerScaleBeanConfig();
    }

    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public EagerScaleBeanConfigurer maxNumberOfContainers(int numberOfContainers) {
        state.setMaxNumberOfContainers(numberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public EagerScaleBeanConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        state.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public EagerScaleBeanConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        state.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }
    
    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public EagerScaleBeanConfig getConfig() {
        return state;
    }
}
