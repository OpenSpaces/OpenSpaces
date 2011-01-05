package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;

/**
 * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
 * 
 * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
 * 
 * @author itaif
 * 
 */
public class ManualCapacityScaleConfigurer implements ScaleBeanConfigurer<ManualCapacityScaleConfig>{

private final ManualCapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
     * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
     * The default constructor wraps an empty {@link ManualCapacityScaleConfig} object
     */
    public ManualCapacityScaleConfigurer() {
        this.config = new ManualCapacityScaleConfig();
    }
    
    public ManualCapacityScaleConfigurer memoryCapacity(String memory) {
        config.setMemoryCapacityInMB((int)MemoryUnit.toMegaBytes(memory));
        return this;
    }
    
    public ManualCapacityScaleConfigurer memoryCapacity(int memory, MemoryUnit unit) {
        config.setMemoryCapacityInMB((int)unit.toMegaBytes(memory));
        return this;
    }
   
    /**
     * @see ManualCapacityScaleConfig#setCpuCapacity(double) 
     */
    public ManualCapacityScaleConfigurer cpuCapacity(double cpuCores) {
        config.setCpuCapacity(cpuCores);
        return this;
    }
   
    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public ManualCapacityScaleConfigurer maxNumberOfContainers(int maxNumberOfContainers) {
        config.setMaxNumberOfContainers(maxNumberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersScaleConfig#setMinNumberOfContainers(int)
     */
    public ManualCapacityScaleConfigurer minNumberOfContainers(int minNumberOfContainers) {
        config.setMinNumberOfContainers(minNumberOfContainers);
        return this;
    }
    
    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public ManualCapacityScaleConfig getConfig() {
        return config;
    }
}
