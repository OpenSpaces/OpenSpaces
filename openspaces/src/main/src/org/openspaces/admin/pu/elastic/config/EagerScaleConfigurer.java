package org.openspaces.admin.pu.elastic.config;



/**
 * Provides fluent API for creating a new {@link EagerScaleConfig} object.
 * 
 * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).create()}
 * 
 * @see EagerScaleConfigurer
 * @see EagerScaleConfig
 * 
 * @since 8.0
 * @author itaif
 * 
 */
public class EagerScaleConfigurer implements ScaleStrategyConfigurer<EagerScaleConfig> {

    private final EagerScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link EagerScaleConfig} object.
     * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).create()}
     * The default constructor wraps an empty {@link EagerScaleConfig} object
     */
    public EagerScaleConfigurer() {
        this.config = new EagerScaleConfig();
    }
    
    public EagerScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }
    
    /**
     * @see EagerScaleConfig#setAtMostOneContainersPerMachine(boolean)
     */
    public EagerScaleConfigurer atMostOneContainerPerMachine() {
        config.setAtMostOneContainerPerMachine(true);
        return this;
    }

    public EagerScaleConfig create() {
        return config;
    }
    
    
}
