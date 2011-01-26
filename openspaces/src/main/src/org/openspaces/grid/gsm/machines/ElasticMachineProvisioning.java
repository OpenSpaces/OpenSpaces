package org.openspaces.grid.gsm.machines;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * An Elastic Service Manager plugin that enables automatic Virtual Machine provisioning.
 * The plugin must be stateless, and is expected to delegate calls to a remove service (such as a cloud).
 * Calls to this class can be concurrent from different threads, therefore implementation must be thread safe.
 * 
 * @author itaif
 * 
 * @see NonBlockingElasticMachineProvisioning
 * @see org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig
 *
 */
public interface ElasticMachineProvisioning {

	/**
	 * Starts a new machine with a new grid service agent with the specified zone.
	 * The zone parameter should also be used to configure the machine with the correct firewall settings.
	 * 
	 * This method is blocking on the current thread, or raises a TimeOutException if the timeout expired.
	 * 
	 * @param capacityRequirements - the capacity requirements that indicate how many machines to start.
	 * @param duration - the maximum duration after which a TimeoutException is raised.
	 * @param unit - the time unit for the duration
	 * @return the grid service agent
	 * 
	 * @throws ElasticMachineProvisioningException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	GridServiceAgent[] startMachines(
			CapacityRequirements capacityRequirements,
			long duration, TimeUnit unit)
	throws ElasticMachineProvisioningException, InterruptedException , TimeoutException ;
	

	/**
	 * Shuts down the grid service agent.
	 * 
	 * This method is blocking on the current thread, or raises a TimeOutException if the timeout expired.
	 * 
	 * The decision whether to terminate also the machine is implementation dependent.
	 * In case the implementation also terminates the machine, the implementation should also keep an independent
	 * scheduled cleanup task to search and terminate orphan machines that do not have a grid service agent running, 
	 * and that have been running for enough time not to suspect that they have just been started.   
	 * 
	 * @param agent
	 * @param duration
	 * @param unit
	 * @return true if grid service agent was shutdown (and depending on the implementation also the machine was terminated)
	 *         false if no action was taken.
	 * @throws TimeoutException - terminating the machine took too long
	 * @throws InterruptedException 
	 * @throws ElasticMachineProvisioningException - terminating the machine encountered a problem. 
	 */
	boolean stopMachine(GridServiceAgent agent, long duration, TimeUnit unit) throws ElasticMachineProvisioningException, InterruptedException, TimeoutException;
	
}