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
import java.util.Map;
import java.util.Map.Entry;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;
import org.openspaces.admin.zone.config.AnyZonesConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfigurer;
import org.openspaces.admin.zone.config.RequiredZonesConfig;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.machines.plugins.discovered.DiscoveredMachineProvisioningBean;

/**
 * Allows to configure an Elastic Processing Unit machine provisioning that discovers existing machines.
 * @author itaif
 * @since 8.0.1
 * @see DiscoveredMachineProvisioningConfigurer
 * @see ElasticDeploymentTopology#dedicatedMachineProvisioning(org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig)
 * @see ElasticDeploymentTopology#sharedMachineProvisioning(String, org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig)
  */
public class DiscoveredMachineProvisioningConfig implements ElasticMachineProvisioningConfig {

    private static final String MINIMUM_NUMBER_OF_CPU_CORES_PER_MACHINE_KEY = "minimum-number-of-cpu-cores-per-machine";
    private static final String MACHINE_AGENT_ZONES_KEY = "machine-agent-zones";
    private static final String[] MACHINE_AGENT_ZONES_DEFAULT = new String[] {};
    private static final String RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_KEY = "reserved-memory-capacity-per-machine-megabytes";
    private static final long RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_DEFAULT = 1024;
    private static final String DEDICATED_MANAGEMENT_MACHINES_KEY = "dedicated-management-machines";
    private static final boolean DEDICATED_MANAGEMENT_MACHINES_DEFAULT = false;
    private static final boolean MACHINE_AGENT_ZONES_MANDATORY_DEFAULT = false;
    private static final String MACHINE_AGENT_ZONES_MANDATORY_KEY = "machine-agent-zones-is-mandatory";
    private static final String RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_KEY = "resereved-drives-capacity-per-machine-megabytes";
    private static final Map<String, String> RESERVED_DRIVES_CAPACITY_PER_MACHINE_DEFAULT = new HashMap<String,String>();
    private static final String RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_KEY_VALUE_SEPERATOR = "=";
    private static final String RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_PAIR_SEPERATOR = ",";
    private static final String RESERVED_CPU_PER_MACHINE_KEY = "reserved-cpu-cores-per-machine";
    private static final double RESERVED_CPU_PER_MACHINE_DEFAULT = 0.0;
    
    StringProperties properties = new StringProperties();
    
    public DiscoveredMachineProvisioningConfig(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public DiscoveredMachineProvisioningConfig() {
        this(new HashMap<String,String>());
    }

    public String getBeanClassName() {
        return DiscoveredMachineProvisioningBean.class.getName();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public Map<String, String> getProperties() {
        return this.properties.getProperties();
    }

    public double getMinimumNumberOfCpuCoresPerMachine() {
        return this.properties.getDouble(MINIMUM_NUMBER_OF_CPU_CORES_PER_MACHINE_KEY, 0.0);
    }

    /**
     * Sets an assessment for the minimum number of CPU cores per machine.
     * 
     * If not specified Processing Unit deployment calls {@link #detectMinimumNumberOfCpuCoresPerMachine}
     * to get an assessment of minimum number of CPU cores per machine.
     * 
     * @since 8.0.0
     */
    public void setMinimumNumberOfCpuCoresPerMachine(double minimumNumberOfCpuCoresPerMachine) {
        this.properties.putDouble(MINIMUM_NUMBER_OF_CPU_CORES_PER_MACHINE_KEY, minimumNumberOfCpuCoresPerMachine);       
    }
    
    public RequiredZonesConfig getGridServiceAgentZones() {
        String[] zones = this.properties.getArray(MACHINE_AGENT_ZONES_KEY, RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_PAIR_SEPERATOR, MACHINE_AGENT_ZONES_DEFAULT);
        if (zones.length == 0) {
            return new AnyZonesConfig();
        }
        return new AtLeastOneZoneConfigurer().addZones(zones).create();
    }
    
    
    /**
     * Sets the list of zones that can be discovered and started by this machine provisioning.
     * 
     * For example:
     * String[] {}                  - Grid Service Agents are started without -Dcom.gs.zones (requires that {@link #isGridServiceAgentZoneMandatory()} is false)
     * String[] {"zoneA"}           - Grid Service Agents are started with -Dcom.gs.zones=zoneA (or without -Dcom.gs.zones if {@link #isGridServiceAgentZoneMandatory()} is false)
     * String[] {"zoneA","zoneB"}   - Grid Service Agents are started with -Dcom.gs.zones=zoneA or -Dcom.gs.zones=zoneB or -Dcom.gs.zones=zoneA,zoneB (or without -Dcom.gs.zones if {@link #isGridServiceAgentZoneMandatory()} is false)
     * 
     * @since 8.0.1
     * 
     */
    public void setGridServiceAgentZones(String[] zones) {
        this.properties.putArray(MACHINE_AGENT_ZONES_KEY, zones, RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_PAIR_SEPERATOR);
    }
    
    /**
     * Sets the expected amount of memory per machine that is reserved for processes other than grid containers.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * Default value is 1024 MB. 
     * 
     * For example, by default, a 16GB server  
     * can run 3 containers 5GB each, since it approximately leaves 1024MB memory free.
     * 
     * @param reservedInMB - amount of reserved memory in MB
     * 
     * @since 8.0.1
     * @see #setReservedCapacityPerMachine(CapacityRequirements)
     */
    public void setReservedMemoryCapacityPerMachineInMB(long reservedInMB) {
        this.properties.putLong(RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_KEY,reservedInMB);
    }
    
    public Map<String,Long> getReservedDriveCapacityPerMachineInMB() {
        Map<String,String> reserved = 
            this.properties.getKeyValuePairs(
                    RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_KEY, RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_PAIR_SEPERATOR, RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_KEY_VALUE_SEPERATOR, RESERVED_DRIVES_CAPACITY_PER_MACHINE_DEFAULT);
        
        Map<String,Long> reservedInMB = new HashMap<String,Long>();
        for(Entry<String, String> pair : reserved.entrySet()) {
            String drive = pair.getKey();
            reservedInMB.put(drive, Long.valueOf(pair.getValue()));
        }
        
        return reservedInMB;
        
    }
    
    /**
     * Sets the expected amount of disk drive size per machine that is reserved for processes other than grid containers.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * Default value is 0 MB. 
     * 
     * @param reservedInMB - amount of reserved disk drive in MB
     * 
     * @since 8.0.3
     * @see #setReservedCapacityPerMachine(CapacityRequirements)
     */
    public void setReservedDriveCapacityPerMachineInMB(Map<String,Long> reservedInMB) {
        Map<String,String> reservedInString = new HashMap<String,String>();
        for(Entry<String, Long> pair : reservedInMB.entrySet()) {
            String drive = pair.getKey();
            reservedInString.put(drive, pair.getValue().toString());
        }
        this.properties.putKeyValuePairs(RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_KEY, reservedInString, RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_PAIR_SEPERATOR, RESREVED_DRIVES_CAPACITY_MEGABYTES_PER_MACHINE_KEY_VALUE_SEPERATOR);
    }
    
    public long getReservedMemoryCapacityPerMachineInMB() {
        return this.properties.getLong(RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_KEY, RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_DEFAULT);
    }

    public double getReservedCpuCapacityPerMachine() {
        return this.properties.getDouble(RESERVED_CPU_PER_MACHINE_KEY, RESERVED_CPU_PER_MACHINE_DEFAULT);
    }
    

    /**
     * Sets the expected CPU cores per machine that is reserved for processes other than grid containers.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * Default value is 0 cpu cores. 
     * 
     * @param reservedCpu - number of reserved CPU cores
     * 
     * @since 8.0.3
     * @see #setReservedCapacityPerMachine(CapacityRequirements)
     */
    public void setReservedCpuCapacityPerMachineInMB(double reservedCpu) {
        this.properties.putDouble(RESERVED_CPU_PER_MACHINE_KEY, reservedCpu);
    }
    
    public CapacityRequirements getReservedCapacityPerMachine() {
        List<CapacityRequirement> requirements = new ArrayList<CapacityRequirement>();
        requirements.add(new MemoryCapacityRequirement(getReservedMemoryCapacityPerMachineInMB()));
        requirements.add(new CpuCapacityRequirement(getReservedCpuCapacityPerMachine()));
        Map<String,Long> reservedDriveCapacity = getReservedDriveCapacityPerMachineInMB();
        for (Entry<String, Long> pair : reservedDriveCapacity.entrySet()) {
            String drive = pair.getKey();
            requirements.add(new DriveCapacityRequirement(drive,pair.getValue()));
        }
        return new CapacityRequirements(requirements.toArray(new CapacityRequirement[requirements.size()]));
    }
    
    /**
     * Sets the expected amount of memory, cpu, drive space (per machine) that is reserved for processes other than processing units.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * Default value is 1024 MB RAM. For example, by default, a 16GB server  
     * can run 3 containers 5GB each, since it approximately leaves 1024MB memory free.
     * 
     * @param capacityRequirements - specifies the reserved memory/cpu/disk space
     * 
     * @since 8.0.3
     * @see #setReservedCpuCapacityPerMachineInMB(double)
     * @see #setReservedMemoryCapacityPerMachineInMB(long)
     * @see #setReservedDriveCapacityPerMachineInMB(Map)
     */
    public void setReservedCapacityPerMachine(CapacityRequirements capacityRequirements) {
        
        MemoryCapacityRequirement memoryCapacityRequirement = capacityRequirements.getRequirement(new MemoryCapacityRequirement().getType());
        if (!memoryCapacityRequirement.equalsZero()) {
            setReservedMemoryCapacityPerMachineInMB(memoryCapacityRequirement.getMemoryInMB());
        }
        
        CpuCapacityRequirement cpuCapacityRequirement = capacityRequirements.getRequirement(new CpuCapacityRequirement().getType());
        if (!memoryCapacityRequirement.equalsZero()) {
            setReservedCpuCapacityPerMachineInMB(cpuCapacityRequirement.getCpu().doubleValue());
        }
        
        Map<String, Long> reservedInMB = new HashMap<String,Long>();
        for (CapacityRequirement requirement : capacityRequirements.getRequirements()) {
            if (requirement instanceof DriveCapacityRequirement) {
                DriveCapacityRequirement driveRequirement = (DriveCapacityRequirement)requirement;
                reservedInMB.put(driveRequirement.getDrive(),driveRequirement.getDriveCapacityInMB());
            }
        }
        setReservedDriveCapacityPerMachineInMB(reservedInMB);
    }
    
    public boolean isDedicatedManagementMachines() {
        return this.properties.getBoolean(DEDICATED_MANAGEMENT_MACHINES_KEY, DEDICATED_MANAGEMENT_MACHINES_DEFAULT);
    }
    
    /**
     * A false value indicates that Grid Service Agents may run a management process. 
     * True indicates that agents started and discovered by this machine provisioning 
     * cannot run a {@link org.openspaces.admin.gsm.GridServiceManager} nor {@link org.openspaces.admin.lus.LookupService} 
     * nor {@link org.openspaces.admin.esm.ElasticServiceManager}
     * 
     * Usually setting this value to true means that {@link #setReservedMemoryCapacityPerMachineInMB(long)} can be decreased, 
     * since no memory needs to be reserved for management processes.
     * 
     * @since 8.0.1
     * 
     */
    public void setDedicatedManagementMachines(boolean isDedicatedManagementMachines) {
        this.properties.putBoolean(DEDICATED_MANAGEMENT_MACHINES_KEY, isDedicatedManagementMachines);
    }

    @Deprecated
    public boolean isGridServiceAgentZoneMandatory() {
        return this.properties.getBoolean(MACHINE_AGENT_ZONES_MANDATORY_KEY, MACHINE_AGENT_ZONES_MANDATORY_DEFAULT);
    }
    
    /**
     * A false value indicates that Grid Service Agents without a zone can be started and discovered by this machine provisioning.
     * True indicates that each started or discovered agent much have one or more of the zones described in {@link #setGridServiceAgentZones(String[])}
     * 
     * This flag is deprecated since 9.1.0 since the zones behavior of agents and containers are the same. Meaning
     * an elastic pu that has a zone defined will not deploy on a GSA without zone anyhow.
     * 
     * @since 8.0.1
     */
    @Deprecated
    public void setGridServiceAgentZoneMandatory(boolean zoneMandatory) {
        this.properties.putBoolean(MACHINE_AGENT_ZONES_MANDATORY_KEY, zoneMandatory);
    }
    
    public static double detectMinimumNumberOfCpuCoresPerMachine(Admin admin) {
        // No machineProvisioning is defined means that the server will use whatever machine it could find.
        // so we just go over all machines and calculate the minimum number of cpu cores per machine.
        final GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
        if (agents.length == 0) {
            throw new AdminException("Cannot determine minimum number of cpu cores per machine. Please use new AdvancedElasticStatefulProcessingUnit().minNumberOfCpuCoresPerMachine() to specify this figure.");
        }
        double minCoresPerMachine = getNumberOfCpuCores(agents[0].getMachine());
        for (final GridServiceAgent agent : agents) {
            final double cores = getNumberOfCpuCores(agent.getMachine());
            if (cores <= 0) {
                throw new AdminException("Cannot determine number of cpu cores on machine " + agent.getMachine().getHostAddress());
            }
            if (minCoresPerMachine < cores) {
                minCoresPerMachine = cores; 
            }
        }
        return minCoresPerMachine;
    }
    
    private static double getNumberOfCpuCores(Machine machine) {
        return machine.getOperatingSystem().getDetails().getAvailableProcessors();
    }
}
