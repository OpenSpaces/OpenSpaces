package org.openspaces.admin.pu.elastic.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscoveredMachineProvisioningConfigurer {

    private DiscoveredMachineProvisioningConfig config;
    private final List<String> agentZones;
    
    public DiscoveredMachineProvisioningConfigurer() {
        this.config = new DiscoveredMachineProvisioningConfig(new HashMap<String,String>());
        this.agentZones = new ArrayList<String>();
    }

    /**
     * @see DiscoveredMachineProvisioningConfig#setMinimumNumberOfCpuCoresPerMachine(double)
     */
    public DiscoveredMachineProvisioningConfigurer minimumNumberOfCpuCoresPerMachine(int minimumNumberOfCpuCoresPerMachine) {
        this.config.setMinimumNumberOfCpuCoresPerMachine(minimumNumberOfCpuCoresPerMachine);
        return this;
    }
    
    /**
     * @see DiscoveredMachineProvisioningConfig#setGridServiceAgentZones(String[])
     */
    public DiscoveredMachineProvisioningConfigurer addGridServiceAgentZone(String zone) {
        this.agentZones.add(zone);
        return this;
    }
    
    public DiscoveredMachineProvisioningConfig create() {
        config.setGridServiceAgentZones(agentZones.toArray(new String[agentZones.size()]));
        return config;
    }
}
