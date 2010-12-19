package org.openspaces.grid.gsm.machines;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.esm.ElasticScaleHandlerException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * 
 * Calls to this class are guaranteed to be called from the same thread.
 * 
 * @author itaif
 *
 */
public interface NonBlockingElasticMachineProvisioning {

	/**
	 * Starts a new machine with a new grid service agent with the specified zone.
	 * The zone parameter should also be used to configure the machine with the correct firewall settings.
	 * 
	 * This method is non blocking and returns a future object with the new grid service agent.
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
	FutureGridServiceAgents startMachinesAsync(
			CapacityRequirements capacityRequirements,
			long duration, TimeUnit unit);
	
	/**
	 * Shuts down the grid service agent or the machine.
	 * 
	 * This method is non blocking and is idempotent.
	 * 
	 * @param agent
	 * @param duration
	 * @param unit
	 * @throws TimeoutException 
	 * @throws InterruptedException 
	 * @throws ElasticScaleHandlerException 
	 */
	void stopMachineAsync(GridServiceAgent agent, long duration, TimeUnit unit);

	
}