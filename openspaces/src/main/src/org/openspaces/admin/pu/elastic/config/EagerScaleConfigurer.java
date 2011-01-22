package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;


/**
 * Provides fluent API for creating a new {@link EagerScaleConfig} object.
 * 
 * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
 * 
 * @see AdvancedEagerScaleConfigurer
 * @see EagerScaleConfig
 * 
 * @since 8.0
 * 
 * @author itaif
 * 
 */
public class EagerScaleConfigurer implements ScaleStrategyConfigurer<EagerScaleConfig> {

    private final EagerScaleConfig config;

    /**
     * Provides fluent API for creating a new {@link EagerScaleConfig} object.
     * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
     * The default constructor wraps an empty {@link EagerScaleConfig} object
     */
    public EagerScaleConfigurer() {
        this.config = new EagerScaleConfig();
    }
    
    /**
     * @see ScaleStrategyConfig#setReservedMemoryCapacityPerMachineInMB(int)
     */
    protected EagerScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB((int) unit.toMegaBytes(memory));
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setAllowDeploymentOnManagementMachine(boolean)
     */
    protected EagerScaleConfigurer allowDeploymentOnManagementMachine(boolean allowDeploymentOnManagementMachine) {
        config.setAllowDeploymentOnManagementMachine(allowDeploymentOnManagementMachine);
        return this;
    }
    
    /**
     * @return 
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    protected EagerScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
     }
    
    /**
     * @see ScaleStrategyConfigurer#getConfig()
     */
    public EagerScaleConfig getConfig() {
        return config;
    }
}
