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
package org.openspaces.grid.gsm.machines.plugins;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgent;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolationAware;

/**
 * An Elastic Service Manager plug-in that enables starting, stopping and discovering of virtual machines.
 * 
 * The plugin must be stateless, and is expected to delegate calls to a remote service (such as a cloud).
 * 
 * Calls to this class are guaranteed to be called from the same thread.
 * 
 * @see ElasticMachineProvisioning
 * @see NonBlockingElasticMachineProvisioningAdapter
 * @see org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig
 * @author itaif
 *
 * @since 8.0
 */
public interface NonBlockingElasticMachineProvisioning extends ElasticProcessingUnitMachineIsolationAware {

    /**
     * @return true if this object supports starting and stopping of machines. 
     * False indicates that {@link #startMachinesAsync(CapacityRequirements, long, TimeUnit)} and {@link #stopMachineAsync(GridServiceAgent, long, TimeUnit)} raise UnsupportedOperationException 
     * 
     * @since 8.0.1
     */
    boolean isStartMachineSupported();

    /**
     * @return a future array of grid service agents that reside in the data center (or cloud region) defined in {@link ElasticMachineProvisioningConfig} 
     * 
     * The caller then filters the result using the criteria defined in {@link ElasticMachineProvisioningConfig#getGridServiceAgentZones()} , 
     * {@link ElasticMachineProvisioningConfig#isGridServiceAgentZoneMandatory()} and {@link ElasticMachineProvisioningConfig#isDedicatedManagementMachines()}
     * so it is not mandatory for the implementation to do so.
     * 
     * @param duration - the maximum duration after which a TimeoutException is raised.
     * @param unit - the time unit for the duration
     * 
     * @since 8.0.1
     */
    
    FutureGridServiceAgents getDiscoveredMachinesAsync(long duration, TimeUnit unit);
    
    /**
     * @return the configuration used by this object
     * @since 8.0.1
     */
    ElasticMachineProvisioningConfig getConfig();

	/**
	 * Starts a new machine with a new grid service agent with the specified zone.
     * All started machines must have Grid Service Agents with zones as defined by {@link ElasticMachineProvisioningConfig#getGridServiceAgentZones()} and 
     * {@link ElasticMachineProvisioningConfig#isGridServiceAgentZoneMandatory()}
	 * 
	 * This method is non blocking and returns a future object with the new grid service agent.
	 * 
	 * @param duration - the maximum duration after which a TimeoutException is raised.
	 * @param unit - the time unit for the duration
	 * @return the grid service agent futures
	 * 
	 * @throws ElasticMachineProvisioningException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * 
	 * @since 8.0
	 */
	FutureGridServiceAgent[] startMachinesAsync(
			CapacityRequirements capacityRequirements,
			long duration, TimeUnit unit);
	
	/**
	 * Shuts down the grid service agent and the machine.
	 * The implementation should be able to close machines that it has not started, but rather an older instance of this object started perhaps with different configuration.
	 * 
	 * This method is non blocking and is idempotent.
	 * 
	 * @param agent
	 * @param duration
	 * @param unit
	 * @throws TimeoutException 
	 * @throws InterruptedException 
	 * @throws ElasticMachineProvisioningException
	 * 
     * @since 8.0 
	 */
	void stopMachineAsync(GridServiceAgent agent, long duration, TimeUnit unit);
	
}
