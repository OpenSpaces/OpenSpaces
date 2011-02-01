package org.openspaces.admin.pu.elastic.config;

import java.util.ArrayList;
import java.util.List;

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
    private final List<String> machineZones;
    
    /**
     * Provides fluent API for creating a new {@link EagerScaleConfig} object.
     * For example {@code new EagerScaleStrategyConfigurer().maxNumberOfContainers(10).create()}
     * The default constructor wraps an empty {@link EagerScaleConfig} object
     */
    public EagerScaleConfigurer() {
        this.config = new EagerScaleConfig();
        this.machineZones = new ArrayList<String>();
    }
    
    public EagerScaleConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB((int) unit.toMegaBytes(memory));
        return this;
    }
    
    public EagerScaleConfigurer dedicatedManagementMachines() {
        config.setDedicatedManagementMachines(true);
        return this;
    }
    
    public EagerScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }
    
    public EagerScaleConfigurer addMachineZone(String machineZone) {
        machineZones.add(machineZone);
        return this;
     }
    
    public EagerScaleConfig create() {
        config.setMachineZones(machineZones.toArray(new String[machineZones.size()]));
        return config;
    }
}
