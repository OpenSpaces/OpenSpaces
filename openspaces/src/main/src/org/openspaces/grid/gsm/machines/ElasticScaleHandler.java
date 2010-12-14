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
public interface ElasticScaleHandler {

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
			String zone, 
			CapacityRequirements capacityRequirements,
			long duration, TimeUnit unit)
	throws ElasticScaleHandlerException, InterruptedException , TimeoutException ;
	

	/**
	 * Shuts down the grid service agent or the machine. 
	 * @param agent
	 * @param duration
	 * @param unit
	 * @return true if grid service agent was shutdown or machine was ordered to start shutdown.
	 * @throws TimeoutException 
	 * @throws InterruptedException 
	 * @throws ElasticScaleHandlerException 
	 */
	boolean stopMachine(GridServiceAgent agent, long duration, TimeUnit unit) throws ElasticScaleHandlerException, InterruptedException, TimeoutException;
	
}