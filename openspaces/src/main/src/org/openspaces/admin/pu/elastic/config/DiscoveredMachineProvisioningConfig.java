package org.openspaces.admin.pu.elastic.config;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;
import org.openspaces.core.util.StringProperties;
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
    private static final String RESERVED_MEMORY_CAPACITY_PER_MACHINE_KEY = "reserved-memory-capacity-per-machine-megabytes";
    private static final long RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_DEFAULT = 1024;
    private static final String DEDICATED_MANAGEMENT_MACHINES_KEY = "dedicated-management-machines";
    private static final boolean DEDICATED_MANAGEMENT_MACHINES_DEFAULT = false;
    private static final boolean MACHINE_AGENT_ZONES_MANDATORY_DEFAULT = false;
    private static final String MACHINE_AGENT_ZONES_MANDATORY_KEY = "machine-agent-zones-is-mandatory";
    
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
    
    public String[] getGridServiceAgentZones() {
        return this.properties.getArray(MACHINE_AGENT_ZONES_KEY, ",", MACHINE_AGENT_ZONES_DEFAULT);
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
        this.properties.putArray(MACHINE_AGENT_ZONES_KEY, zones, ",");
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
     */
    public void setReservedMemoryCapacityPerMachineInMB(long reservedMemoryCapacityPerMachineInMB) {
        this.properties.putLong(RESERVED_MEMORY_CAPACITY_PER_MACHINE_KEY,reservedMemoryCapacityPerMachineInMB);
    }
    
    public long getReservedMemoryCapacityPerMachineInMB() {
        return this.properties.getLong(RESERVED_MEMORY_CAPACITY_PER_MACHINE_KEY, RESERVED_MEMORY_CAPACITY_PER_MACHINE_MEGABYTES_DEFAULT);
    }

    public boolean isDedicatedManagementMachines() {
        return this.properties.getBoolean(DEDICATED_MANAGEMENT_MACHINES_KEY, DEDICATED_MANAGEMENT_MACHINES_DEFAULT);
    }
    
    /**
     * A false value indicates that Grid Service Agents may run a management process. 
     * True indicates that agents started and discovered by this machine provisioning 
     * cannot run a {@link org.openspaces.admin.gsm.GridServiceManager} nor {@link org.openspaces.admin.lus.LookupService} 
     * nor {@link org.openspaces.admin.gsm.ElasticServiceManager}
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

    public boolean isGridServiceAgentZoneMandatory() {
        return this.properties.getBoolean(MACHINE_AGENT_ZONES_MANDATORY_KEY, MACHINE_AGENT_ZONES_MANDATORY_DEFAULT);
    }
    
    /**
     * A false value indicates that Grid Service Agents without a zone can be started and discovered by this machine provisioning.
     * True indicates that each started or discovered agent much have one or more of the zones described in {@link #setGridServiceAgentZones(String[]))}
     * 
     * @since 8.0.1
     */
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