package org.openspaces.admin.pu.elastic.config;

/**
 * Provides fluent API for creating a new {@link EagerScaleConfig} object.
 * 
 * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
 * 
 * @author itaif
 * 
 */
public class EagerScaleConfigurer implements ScaleBeanConfigurer<EagerScaleConfig> {

    private final EagerScaleConfig state;

    /**
     * Provides fluent API for creating a new {@link EagerScaleConfig} object.
     * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
     * The default constructor wraps an empty {@link EagerScaleConfig} object
     */
    public EagerScaleConfigurer() {
        this.state = new EagerScaleConfig();
    }

    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public EagerScaleConfigurer maxNumberOfContainers(int numberOfContainers) {
        state.setMaxNumberOfContainers(numberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public EagerScaleConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        state.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public EagerScaleConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        state.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }
    
    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public EagerScaleConfig getConfig() {
        return state;
    }
}
