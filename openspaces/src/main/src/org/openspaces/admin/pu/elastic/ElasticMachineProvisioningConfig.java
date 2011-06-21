package org.openspaces.admin.pu.elastic;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * Defines the required configuration properties of an @{link ElasticMachineProvisioning} bean.
 * This bean 
 * 
 * @author itaif
 * 
 * @since 8.0.0
 */
public interface ElasticMachineProvisioningConfig extends BeanConfig {
    

    /**
     * Gets the minimum number of CPU cores per machine.
     * 
     * This value is used during deployment to calculate number of partitions (partitions = maxNumberOfCpuCores/minNumberOfCpuCoresPerMachine)
     * 
     * @since 8.0.0
     */
    double getMinimumNumberOfCpuCoresPerMachine();
    
    /**
     * Gets the expected amount of memory,cpu,disk,etc... per machine that is reserved for processes other than grid containers.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * For example, a machine with a 16GB server and 1GB reserved can run 3 containers 5GB each.
     * 
     * @since 8.0.2
     */
    CapacityRequirements getReservedCapacityPerMachine();
    
    /**
     * Gets the list of zones that can be discovered and started by this machine provisioning.
     * By default returns an empty array.
     * 
     * For example:
     * String[] {}                  - Grid Service Agents are started without -Dcom.gs.zones (requires that {@link #isGridServiceAgentZoneMandatory()} is false)
     * String[] {"zoneA"}           - Grid Service Agents are started with -Dcom.gs.zones=zoneA (or without -Dcom.gs.zones if {@link #isGridServiceAgentZoneMandatory()} is false)
     * String[] {"zoneA","zoneB"}   - Grid Service Agents are started with -Dcom.gs.zones=zoneA or -Dcom.gs.zones=zoneB or -Dcom.gs.zones=zoneA,zoneB (or without -Dcom.gs.zones if {@link #isGridServiceAgentZoneMandatory()} is false)
     * 
     * @since 8.0.1
     * 
     */
    public String[] getGridServiceAgentZones();
    
    /**
     * By default is false, which means that a Grid Service Agents may run a management process. 
     * If true, it means that agents started and discovered by this machine provisioning 
     * cannot run a {@link org.openspaces.admin.gsm.GridServiceManager} nor {@link org.openspaces.admin.lus.LookupService} 
     * nor {@link org.openspaces.admin.esm.ElasticServiceManager}
     * 
     * Usually setting this value to true means that {@link #getReservedCapacityPerMachineInMB()} memory can be decreased, 
     * since no memory needs to be reserved for management processes.
     * 
     * @since 8.0.1
     * 
     */
    boolean isDedicatedManagementMachines();
    
    /**
     * By default is false, which means that Grid Service Agents without a zone can be started and discovered by this machine provisioning.
     * When true, each started or discovered agent much have one or more of the zones described in {@link #getGridServiceAgentZones()}
     * 
     * @since 8.0.1
     */
    boolean isGridServiceAgentZoneMandatory();

}
