package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;

/**
 * This Advanced version of {@link ManualCapacityScaleConfigurer} allows 
 * implementation related tweaking that might change in the future.
 *   
 * @see ManualCapacityScaleConfigurer
 * @author itaif
 * @since 8.0
 */
public class AdvancedManualCapacityScaleConfigurer extends ManualCapacityScaleConfigurer{

    /**
     * @see ManualCapacityScaleConfig#setMemoryCapacityInMB(int)
     */
    public AdvancedManualCapacityScaleConfigurer memoryCapacity(String memory) {
        super.memoryCapacity(memory);
        return this;
    }

    /**
     * @see ManualCapacityScaleConfig#setMemoryCapacityInMB(int)
     */
    public AdvancedManualCapacityScaleConfigurer memoryCapacity(int memory, MemoryUnit unit) {
        super.memoryCapacity(memory, unit);
        return this;
    }
   
    /**
     * @see ManualCapacityScaleConfig#setNumberOfCpuCores(double) 
     */
    public AdvancedManualCapacityScaleConfigurer numberOfCpuCores(double cpuCores) {
        super.numberOfCpuCores(cpuCores);
        return this;
    }
   
    /**
     * @see ScaleStrategyConfig#setReservedMemoryCapacityPerMachineInMB(int)
     */
    public AdvancedManualCapacityScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        super.reservedMemoryCapacityPerMachine(memory, unit);
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setDedicatedManagementMachines(boolean)
     */
    public AdvancedManualCapacityScaleConfigurer dedicatedManagementMachines() {
        super.dedicatedManagementMachines();
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    public AdvancedManualCapacityScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        super.maxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }
}
