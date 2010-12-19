package org.openspaces.grid.gsm.machines;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.esm.ElasticScaleHandlerException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * Calls to this class can be concurrent from different threads, therefore implementation must be thread safe.
 * @author itaif
 *
 */
public interface ElasticMachineProvisioning {

	/**
	 * Starts a new machine with a new grid service agent with the specified zone.
	 * The zone parameter should also be used to configure the machine with the correct firewall settings.
	 * 
	 * This method is blocking on the current thread, or raises a TimeOutException if the timeout expired.
	 * 
	 * @param zone - the grid service agent's zone.
	 * @param requirements - the capacity requirements that indicate how many machines to start.
	 * @param duration - the maximum duration after which a TimeoutException is raised.
	 * @return the grid service agent
	 * 
	 * @throws ElasticScaleHandlerException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	GridServiceAgent[] startMachines(
			CapacityRequirements capacityRequirements,
			long duration, TimeUnit unit)
	throws ElasticScaleHandlerException, InterruptedException , TimeoutException ;
	

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
	 * @throws ElasticScaleHandlerException - terminating the machine encountered a problem. 
	 */
	boolean stopMachine(GridServiceAgent agent, long duration, TimeUnit unit) throws ElasticScaleHandlerException, InterruptedException, TimeoutException;
	
}