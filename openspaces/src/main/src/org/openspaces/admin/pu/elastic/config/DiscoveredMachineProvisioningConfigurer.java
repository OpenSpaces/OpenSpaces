package org.openspaces.admin.pu.elastic.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openspaces.core.util.MemoryUnit;

public class DiscoveredMachineProvisioningConfigurer {

    private DiscoveredMachineProvisioningConfig config;
    private final List<String> agentZones;
    
    public DiscoveredMachineProvisioningConfigurer() {
        config = new DiscoveredMachineProvisioningConfig(new HashMap<String,String>());
        agentZones = new ArrayList<String>();
    }

    /**
     * @see DiscoveredMachineProvisioningConfig#setMinimumNumberOfCpuCoresPerMachine(double)
     */
    public DiscoveredMachineProvisioningConfigurer minimumNumberOfCpuCoresPerMachine(int minimumNumberOfCpuCoresPerMachine) {
        config.setMinimumNumberOfCpuCoresPerMachine(minimumNumberOfCpuCoresPerMachine);
        return this;
    }
    
    /**
     * @see DiscoveredMachineProvisioningConfig#setGridServiceAgentZones(String[])
     */
    public DiscoveredMachineProvisioningConfigurer addGridServiceAgentZone(String zone) {
        agentZones.add(zone);
        return this;
    }
    
    /**
     * @see DiscoveredMachineProvisioningConfig#setGridServiceAgentZoneMandatory(boolean)
     */
    public DiscoveredMachineProvisioningConfigurer removeGridServiceAgentsWithoutZone() {
        config.setGridServiceAgentZoneMandatory(true);
        return this;
    }
    
    /**
     * @see DiscoveredMachineProvisioningConfig#setReservedMemoryCapacityPerMachineInMB(long)
     */
    public DiscoveredMachineProvisioningConfigurer reservedMemoryCapacityPerMachine(long memory, MemoryUnit unit) {
        config.setReservedMemoryCapacityPerMachineInMB(MemoryUnit.MEGABYTES.convert(memory, unit));
        return this;
    }
    
    /**
     * @see  DiscoveredMachineProvisioningConfig#setReservedMemoryCapacityPerMachineInMB(long)
     */
    public DiscoveredMachineProvisioningConfigurer reservedMemoryCapacityPerMachine(String memory) {
        config.setReservedMemoryCapacityPerMachineInMB(MemoryUnit.MEGABYTES.convert(memory));
        return this;
    }
    
    /**
     * @see  DiscoveredMachineProvisioningConfig#setDedicatedManagementMachines(boolean)
     */
    public DiscoveredMachineProvisioningConfigurer dedicatedManagementMachines() {
        config.setDedicatedManagementMachines(true);
        return this;
    }
    
    public DiscoveredMachineProvisioningConfig create() {
        config.setGridServiceAgentZones(agentZones.toArray(new String[agentZones.size()]));
        return config;
    }
}
