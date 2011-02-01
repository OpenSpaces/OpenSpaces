package org.openspaces.admin.pu.elastic.config;

import java.util.ArrayList;
import java.util.List;

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
    private final List<String> machineZones;
    
    /**
     * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
     * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().memoryCapacity("1500m").create()}
     * The default constructor wraps an empty {@link ManualCapacityScaleConfig} object
     */
    public ManualCapacityScaleConfigurer() {
        this.config = new ManualCapacityScaleConfig();
        this.machineZones = new ArrayList<String>();
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
   
    public ManualCapacityScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB((int) unit.toMegaBytes(memory));
        return this;
    }
    
    public ManualCapacityScaleConfigurer dedicatedManagementMachines() {
        config.setDedicatedManagementMachines(true);
        return this;
    }
    
    public ManualCapacityScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }
    
    public ManualCapacityScaleConfigurer addMachineZone(String machineZone) {
        machineZones.add(machineZone);
        return this;
     }
    
    public ManualCapacityScaleConfig create() {
        config.setMachineZones(this.machineZones.toArray(new String[machineZones.size()]));
        return config;
    }
}
