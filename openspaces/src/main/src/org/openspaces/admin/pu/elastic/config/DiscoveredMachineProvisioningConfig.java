package org.openspaces.admin.pu.elastic.config;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.machines.DefaultMachineProvisioning;

public class DiscoveredMachineProvisioningConfig implements ElasticMachineProvisioningConfig {

    private static final String MINIMUM_NUMBER_OF_CPU_CORES_PER_MACHINE_KEY = "minimum-number-of-cpu-cores-per-machine";
    private static final String MACHINE_ZONES_KEY = "machine-zones";
    StringProperties properties = new StringProperties();
    
    public DiscoveredMachineProvisioningConfig(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public String getBeanClassName() {
        return DefaultMachineProvisioning.class.getName();
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
     * Overrides the minimum number of CPU cores per machine assumption.
     * 
     * If not specified the minimum number of CPU cores per machine 
     * then Processing Unit deployment may call {@link #detectMinimumNumberOfCpuCoresPerMachine}
     * to get an assessment of minimum number of CPU cores per machine.
     * 
     * @since 8.0.1
     */
    public void setMinimumNumberOfCpuCoresPerMachine(double minimumNumberOfCpuCoresPerMachine) {
        this.properties.putDouble(MINIMUM_NUMBER_OF_CPU_CORES_PER_MACHINE_KEY, minimumNumberOfCpuCoresPerMachine);
    }
    
    public String[] getGridServiceAgentZones() {
        return this.properties.getArray(MACHINE_ZONES_KEY, ",", new String[]{});
    }
    
    /**
     * Specifies a list of machine zones that are discovered.
     * A machine is discovered if an agent with one or more of the specified zones, 
     * or an agent without any zones at all is discovered on the machine. 
     * 
     * In case the specified zones list is empty, then any machine with an agent is discovered (the default behavior)
     * 
     * @since 8.0.1
     * 
     */
    public void setGridServiceAgentZones(String[] zones) {
        this.properties.putArray(MACHINE_ZONES_KEY,zones,",");
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