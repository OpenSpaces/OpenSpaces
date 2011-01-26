package org.openspaces.grid.gsm.machines;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * An Elastic Service Manager plugin that enables automatic Virtual Machine provisioning.
 * The plugin must be stateless, and is expected to delegate calls to a remove service (such as a cloud).
 * 
 * Calls to this class are guaranteed to be called from the same thread.
 * 
 * @see ElasticMachineProvisioning
 * @see NonBlockingElasticMachineProvisioningAdapter
 * @see org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig
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
	 * @param duration - the maximum duration after which a TimeoutException is raised.
	 * @param unit - the time unit for the duration
	 * @return the grid service agent
	 * 
	 * @throws ElasticMachineProvisioningException
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
	 * @throws ElasticMachineProvisioningException 
	 */
	void stopMachineAsync(GridServiceAgent agent, long duration, TimeUnit unit);

	
}