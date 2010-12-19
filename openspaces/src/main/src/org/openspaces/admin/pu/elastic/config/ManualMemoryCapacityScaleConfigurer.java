package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;

/**
 * Provides fluent API for creating a new {@link ManualMemoryCapacityScaleConfig} object.
 * 
 * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
 * 
 * @author itaif
 * 
 */
public class ManualMemoryCapacityScaleConfigurer implements ScaleBeanConfigurer<ManualMemoryCapacityScaleConfig>{

private final ManualMemoryCapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link ManualMemoryCapacityScaleConfig} object.
     * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
     * The default constructor wraps an empty {@link ManualMemoryCapacityScaleConfig} object
     */
    public ManualMemoryCapacityScaleConfigurer() {
        this.config = new ManualMemoryCapacityScaleConfig();
    }
    
    public ManualMemoryCapacityScaleConfigurer memoryCapacity(String memory) {
        config.setMemoryCapacityInMB((int)MemoryUnit.toMegaBytes(memory));
        return this;
    }
    
    public ManualMemoryCapacityScaleConfigurer memoryCapacity(int memory, MemoryUnit unit) {
        config.setMemoryCapacityInMB((int)unit.toMegaBytes(memory));
        return this;
    }
        
    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public ManualMemoryCapacityScaleConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public ManualMemoryCapacityScaleConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        config.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public ManualMemoryCapacityScaleConfigurer maxNumberOfContainers(int maxNumberOfContainers) {
        config.setMaxNumberOfContainers(maxNumberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersScaleConfig#setMinNumberOfContainers(int)
     */
    public ManualMemoryCapacityScaleConfigurer minNumberOfContainers(int minNumberOfContainers) {
        config.setMinNumberOfContainers(minNumberOfContainers);
        return this;
    }
    
    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public ManualMemoryCapacityScaleConfig getConfig() {
        return config;
    }
}
