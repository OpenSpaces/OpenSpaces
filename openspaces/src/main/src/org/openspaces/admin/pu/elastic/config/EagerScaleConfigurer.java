package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;

/**
 * Provides fluent API for creating a new {@link EagerScaleConfig} object.
 * 
 * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
 * 
 * @author itaif
 * 
 */
public class EagerScaleConfigurer implements ScaleBeanConfigurer<EagerScaleConfig> {

    private final EagerScaleConfig config;

    /**
     * Provides fluent API for creating a new {@link EagerScaleConfig} object.
     * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).getConfig()}
     * The default constructor wraps an empty {@link EagerScaleConfig} object
     */
    public EagerScaleConfigurer() {
        this.config = new EagerScaleConfig();
    }

    public EagerScaleConfigurer reservedMemoryCapacityPerMachine(int memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB((int) unit.toMegaBytes(memory));
        return this;
    }
    
    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public EagerScaleConfig getConfig() {
        return config;
    }
}
