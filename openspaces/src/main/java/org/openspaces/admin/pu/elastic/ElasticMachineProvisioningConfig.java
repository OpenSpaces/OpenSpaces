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
package org.openspaces.admin.pu.elastic;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
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
     * If Grid Service Agents can be with/without any zone
     * return new AnyZonesConfig()
     *  
     * If Grid Service Agents must have at least "zoneA" (started with -Dcom.gs.zones=zoneA)
     * return new AtLeastOneZoneConfigurer().addZone("zoneA").create()
     * 
     * If Grid Service Agents must have at least "zoneA" or "zoneB" (or both)
     * return new AtLeastOneZoneConfigurer().addZones("zoneA","zoneB").create()
     * 
     * If Grid Service Agents must have exactly "zoneA" and "zoneB" (and no other zone)
     * return new ExactOneZoneConfigurer().addZones("zoneA","zoneB").create()
     * 
     * @since 8.0.1
     */
    public ZonesConfig getGridServiceAgentZones();
    
    /**
     * By default is false, which means that a Grid Service Agents may run a management process. 
     * If true, it means that agents started and discovered by this machine provisioning 
     * cannot run a {@link org.openspaces.admin.gsm.GridServiceManager} nor {@link org.openspaces.admin.lus.LookupService} 
     * nor {@link org.openspaces.admin.esm.ElasticServiceManager}
     * 
     * Usually setting this value to true means that {@link #getReservedCapacityPerMachine()} memory can be decreased, 
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
     * This flag is deprecated since 9.1.0 since the zones behavior of agents and containers are the same. Meaning
     * an elastic pu that has a zone defined will not deploy on a GSA without zone anyhow.
     * 
     * @since 8.0.1
     */
    @Deprecated
    boolean isGridServiceAgentZoneMandatory();

    /**
     * Gets the expected amount of memory,cpu,disk,etc... per management machine that is reserved for processes other than grid containers.
     * These include Grid Service Manager, Lookup Service or any other daemon running on the system.
     * 
     * For example, a machine with a 16GB server and 1GB reserved can run 3 containers 5GB each.
     * 
     * @since 9.5.0
     */
	CapacityRequirements getReservedCapacityPerManagementMachine();
}
