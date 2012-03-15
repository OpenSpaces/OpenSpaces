/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.pu.elastic.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

/**
 * Allows to configure an Elastic Processing Unit machine provisioning that discovers existing machines.
 * @author itaif
 * @since 8.0.1
 * @see DiscoveredMachineProvisioningConfig
 * @see ElasticDeploymentTopology#dedicatedMachineProvisioning(org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig)
 * @see ElasticDeploymentTopology#sharedMachineProvisioning(String, org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig)
  */
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
