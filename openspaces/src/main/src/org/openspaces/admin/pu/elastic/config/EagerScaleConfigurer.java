package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;


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
    
    /**
     * @see ScaleStrategyConfig#setReservedMemoryCapacityPerMachineInMB(int)
     */
    public EagerScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB((int) unit.toMegaBytes(memory));
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setDedicatedManagementMachines(boolean)
     */
    public EagerScaleConfigurer dedicatedManagementMachines() {
        config.setDedicatedManagementMachines(true);
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    public EagerScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
     }
    
    /**
     * @see ScaleStrategyConfigurer#create()
     */
    public EagerScaleConfig create() {
        return config;
    }
}
