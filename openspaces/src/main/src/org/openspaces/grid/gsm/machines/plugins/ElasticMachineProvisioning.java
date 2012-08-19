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
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolationAware;

/**
 * An Elastic Service Manager plug-in that enables starting, stopping and discovering of virtual machines.
 * 
 * All started machines must have Grid Service Agents with zones as defined by {@link ElasticMachineProvisioningConfig#getGridServiceAgentZones()} and 
 * {@link ElasticMachineProvisioningConfig#isGridServiceAgentZoneMandatory()}
 * 
 * The plugin must be stateless, and is expected to delegate calls to a remove service (such as a cloud).
 * Calls to this class can be concurrent from different threads, therefore implementation must be thread safe.
 * 
 * @author itaif
 * 
 * @see NonBlockingElasticMachineProvisioning
 * @see org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig
 * 
 * @since 8.0
 *
 */
public interface ElasticMachineProvisioning extends ElasticProcessingUnitMachineIsolationAware {

    /**
     * @return true if this object supports starting and stopping of machines. 
     * False indicates that {@link #startMachine(long, TimeUnit)} and {@link #stopMachine(GridServiceAgent, long, TimeUnit)} raise UnsupportedOperationException 
     * 
     * @since 8.0.1
     */
    boolean isStartMachineSupported();
    
    /**
     * @return a list of grid service agents that reside in the data center (or cloud region) defined in {@link ElasticMachineProvisioningConfig} 
     * 
     * The caller then filters the result using the criteria defined in {@link ElasticMachineProvisioningConfig#getGridServiceAgentZones()} , 
     * {@link ElasticMachineProvisioningConfig#isGridServiceAgentZoneMandatory()} and {@link ElasticMachineProvisioningConfig#isDedicatedManagementMachines()}
     * so it is not mandatory for the implementation to do so.
     * 
     * @since 8.0.1
     */
    GridServiceAgent[] getDiscoveredMachines(long duration, TimeUnit unit) throws ElasticMachineProvisioningException, InterruptedException, TimeoutException;

	/**
	 * Starts a new machine with a new grid service agent and injects specific zones into it.
	 * 
	 * This method is blocking on the current thread, or raises a TimeOutException if the timeout expired.
	 * 
	 * @param duration - the maximum duration after which a TimeoutException is raised.
	 * @param unit - the time unit for the duration
	 * @param zones - the zones to start the machine on. these zones will be injected into the GSA that is started on this machine.
	 * @return the grid service agent
	 * 
	 * @throws ElasticMachineProvisioningException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * 
	 * @since 8.0
	 */
	GridServiceAgent startMachine(ExactZonesConfig zones, long duration,  TimeUnit unit)
	throws ElasticMachineProvisioningException, InterruptedException , TimeoutException ;

	 /**
     * Starts a new machine with a new grid service agent without specifying zones. the GSA will be assigned only the default zones.
     * 
     * This method is blocking on the current thread, or raises a TimeOutException if the timeout expired.
     * 
     * @param duration - the maximum duration after which a TimeoutException is raised.
     * @param unit - the time unit for the duration
     * @return the grid service agent
     * 
     * @throws ElasticMachineProvisioningException
     * @throws InterruptedException
     * @throws TimeoutException
     * 
     * @since 8.0
     */
	@Deprecated
	GridServiceAgent startMachine(long duration,  TimeUnit unit)
	throws ElasticMachineProvisioningException, InterruptedException , TimeoutException ;
	
	/**
	 * @return the capacity requirements that represent a single machine 
	 */
	CapacityRequirements getCapacityOfSingleMachine();

	/**
	 * Shuts down the grid service agent and the machine.
	 * 
	 * This method is blocking on the current thread, or raises a TimeOutException if the timeout expired.
	 * The implementation should be able to close machines that it has not started, but rather an older instance of this object started perhaps with different configuration.
	 * 
	 * The implementation should also keep an independent scheduled cleanup task to search and terminate 
	 * orphan machines that do not have a grid service agent running, 
	 * and that have been running for enough time not to suspect that they have just been started.   
	 * 
	 * @param agent - the agent to stop
	 * @param duration - timeout duration
	 * @param unit - timeout duration time unit
	 * @return true if grid service agent was shutdown (and depending on the implementation also the machine was terminated)
	 *         false if no action was taken.
	 * @throws TimeoutException - terminating the machine took too long
	 * @throws InterruptedException 
	 * @throws ElasticMachineProvisioningException - terminating the machine encountered a problem.
	 * 
	 * @since 8.0
	 */
	boolean stopMachine(GridServiceAgent agent, long duration, TimeUnit unit) throws ElasticMachineProvisioningException, InterruptedException, TimeoutException;
	
	
    /**
     * @return the configuration used by this object
     * @since 8.0.1
     */
    ElasticMachineProvisioningConfig getConfig();
    
	
}
