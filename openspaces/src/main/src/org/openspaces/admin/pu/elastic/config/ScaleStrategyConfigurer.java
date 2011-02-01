package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfigurer;
import org.openspaces.core.util.MemoryUnit;

public interface ScaleStrategyConfigurer<T extends ScaleStrategyConfig> extends BeanConfigurer<T> {

    /**
     * @see ScaleStrategyConfig#setReservedMemoryCapacityPerMachineInMB(int)
     */
    ScaleStrategyConfigurer<T> reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit);
    
    /**
     * @see ScaleStrategyConfig#setDedicatedManagementMachines(boolean)
     */
    ScaleStrategyConfigurer<T> dedicatedManagementMachines();

    
    /**
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    ScaleStrategyConfigurer<T> maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine);
    
    /**
     * @see ScaleStrategyConfig#setMachineZones(String[])
     */
    ScaleStrategyConfigurer<T> addMachineZone(String machineZone);
}
