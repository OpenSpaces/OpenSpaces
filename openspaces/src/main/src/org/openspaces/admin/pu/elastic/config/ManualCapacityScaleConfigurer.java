package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;

/**
 * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
 * 
 * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
 * 
 * @author itaif
 * @since 8.0
 * @see ManualCapacityScaleConfig
 */
public class ManualCapacityScaleConfigurer implements ScaleStrategyConfigurer<ManualCapacityScaleConfig>{

private final ManualCapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
     * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().capacity("1500m").getConfig()}
     * The default constructor wraps an empty {@link ManualCapacityScaleConfig} object
     */
    public ManualCapacityScaleConfigurer() {
        this.config = new ManualCapacityScaleConfig();
    }
    
    /**
     * @see ManualCapacityScaleConfig#setMemoryCapacityInMB(int)
     */
    public ManualCapacityScaleConfigurer memoryCapacity(String memory) {
        config.setMemoryCapacityInMB(MemoryUnit.toMegaBytes(memory));
        return this;
    }

    /**
     * @see ManualCapacityScaleConfig#setMemoryCapacityInMB(int)
     */
    public ManualCapacityScaleConfigurer memoryCapacity(int memory, MemoryUnit unit) {
        config.setMemoryCapacityInMB(unit.toMegaBytes(memory));
        return this;
    }
   
    /**
     * @see ManualCapacityScaleConfig#setNumberOfCpuCores(double) 
     */
    public ManualCapacityScaleConfigurer numberOfCpuCores(double cpuCores) {
        config.setNumberOfCpuCores(cpuCores);
        return this;
    }
   
    /**
     * @see ScaleStrategyConfig#setReservedMemoryCapacityPerMachineInMB(int)
     */
    protected ManualCapacityScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB((int) unit.toMegaBytes(memory));
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setDedicatedManagementMachines(boolean)
     */
    protected ManualCapacityScaleConfigurer dedicatedManagementMachines() {
        config.setDedicatedManagementMachines(true);
        return this;
    }
    
    /**
     * @return 
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    protected ManualCapacityScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
     }
    
    /**
     * @see ScaleStrategyConfigurer#getConfig()
     */
    public ManualCapacityScaleConfig getConfig() {
        return config;
    }

    
}
