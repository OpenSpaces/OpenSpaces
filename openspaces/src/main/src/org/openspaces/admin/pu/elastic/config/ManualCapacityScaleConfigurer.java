package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.core.util.MemoryUnit;

/**
 * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
 * 
 * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().memoryCapacity("1500m").create()}
 * 
 * @author itaif
 * @since 8.0
 * @see ManualCapacityScaleConfig
 */
public class ManualCapacityScaleConfigurer implements ScaleStrategyConfigurer<ManualCapacityScaleConfig>{

    private final ManualCapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
     * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().memoryCapacity("1500m").create()}
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
     * @see ManualCapacityScaleConfig#setDriveCapacityInMB(String,long) 
     * @since 8.0.2
     */
    public ManualCapacityScaleConfigurer driveCapacity(String drive, int size, MemoryUnit unit) {
        setDriveCapacity(drive, unit.toMegaBytes(size));
        return this;
    }
    
    /**
     * @see ManualCapacityScaleConfig#setDriveCapacityInMB(String,long)
     * @since 8.0.2
     */
    public ManualCapacityScaleConfigurer driveCapacity(String drive, String size) {
        setDriveCapacity(drive, MemoryUnit.toMegaBytes(size));
        return this;
    }

    private void setDriveCapacity(String drive, long sizeInMB) {
        final Map<String, Long> drivesCapacityInMB = config.getDrivesCapacityInMB();
        drivesCapacityInMB.put(drive, sizeInMB);
        config.setDrivesCapacityInMB(drivesCapacityInMB);
    }

    /**
     * @see ManualCapacityScaleConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    public ManualCapacityScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }
        
    public ManualCapacityScaleConfig create() {
        return config;
    }
}
