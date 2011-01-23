package org.openspaces.admin.pu.elastic.config;

import org.openspaces.core.util.MemoryUnit;


/**
 * This Advanced version of {@link EagerScaleConfigurer} allows 
 * implementation related tweaking that might change in the future.
 *   
 * @see EagerScaleConfigurer
 * @see EagerScaleConfig
 * 
 * @author itaif
 * @since 8.0
 */
public class AdvancedEagerScaleConfigurer extends EagerScaleConfigurer {
    
    /**
     * @see ScaleStrategyConfig#setReservedMemoryCapacityPerMachineInMB(int)
     */
    public AdvancedEagerScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        super.reservedMemoryCapacityPerMachine(memory, unit);
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setDedicatedManagementMachines(boolean)
     */
    public AdvancedEagerScaleConfigurer dedicatedManagementMachines() {
        super.dedicatedManagementMachines();
        return this;
    }
    
    /**
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    public AdvancedEagerScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        super.maxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }
    
    
}
