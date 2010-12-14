package org.openspaces.grid.gsm.machines;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.esm.ElasticScaleHandlerException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * An adapter that wraps an {@link ElasticScaleHandler} and exposes a {@link NonBlockingElasticScaleHandler}
 *
 * @author itaif
 */
public class NonBlockingElasticScaleHandlerAdapter implements NonBlockingElasticScaleHandler {

	private ElasticScaleHandler elasticScaleHandler;

	private static final Log logger = LogFactory
			.getLog(NonBlockingElasticScaleHandlerAdapter.class);

	private ExecutorService service = Executors
			.newCachedThreadPool(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					return new Thread(r, this.getClass().getName());
				}

			});

	NonBlockingElasticScaleHandlerAdapter(ElasticScaleHandler machinePool) {
		this.elasticScaleHandler = machinePool;
	}

	public FutureGridServiceAgents startMachinesAsync(
			final String zone,
			final CapacityRequirements capacityRequirements,
			final long duration, final TimeUnit unit) {
		
		final AtomicReference<Object> ref = new AtomicReference<Object>(null);
		final long start = System.currentTimeMillis();
		final long end = start
				+ unit.toMillis(duration);

		service.submit(new Runnable() {
			public void run() {
				try {
					GridServiceAgent[] agents = elasticScaleHandler.startMachines(zone, capacityRequirements, duration, unit);
					ref.set(agents);
				} catch (ElasticScaleHandlerException e) {
					ref.set(new ExecutionException(e));
				} catch (InterruptedException e) {
					ref.set(new ExecutionException(e));
				} catch (Exception e) {
					logger.error("Unexpected exception", e);
					ref.set(new ExecutionException(e));
				}
			}

		});

		FutureGridServiceAgents future = new FutureGridServiceAgents() {

			public boolean isDone() {
				return  System.currentTimeMillis() > end || ref.get() != null;
			}
			
			public ExecutionException getException() {
				Object result = ref.get();
				if (result != null && result instanceof ExecutionException) {
					return (ExecutionException) result;
				}
				return null;
			}

			public boolean isTimedOut() {
				Object result = ref.get();
				return System.currentTimeMillis() > end || 
					   (result != null && 
					    result instanceof ExecutionException && 
					    ((ExecutionException)result).getCause() != null && 
					    ((ExecutionException)result).getCause() instanceof TimeoutException);
			}


			public Date getTimestamp() {
				return new Date(start);
			}

			public GridServiceAgent[] get()
					throws ExecutionException, IllegalStateException,TimeoutException {

				if (System.currentTimeMillis() > end) {
					throw new TimeoutException(
							"Starting a new machine took more than "
									+ unit.toSeconds(duration)
									+ " seconds to complete.");
				}

				Object result = ref.get();

				if (result == null) {
					throw new IllegalStateException(
							"Async operation is not done yet.");
				}

				if (result instanceof Exception) {
					throw new ExecutionException((Exception) result);
				}

				GridServiceAgent[] agents = (GridServiceAgent[]) result;

				return agents;
			}
			
			public CapacityRequirements getCapacityRequirements() {
				return capacityRequirements;
			}
		};

		return future;
	}

	public void stopMachineAsync(final GridServiceAgent agent, final long duration,
			final TimeUnit unit) throws ElasticScaleHandlerException,
			InterruptedException, TimeoutException {

		final String hostAddress = agent.getMachine().getHostAddress();
		
		service.submit(new Runnable() {
			public void run() {
				try {
					NonBlockingElasticScaleHandlerAdapter.this.elasticScaleHandler.stopMachine(agent, duration, unit);
					logger.info(hostAddress + " stopped succesfully.");
				} catch (ElasticScaleHandlerException e) {
					logger.warn("Error while stopping " + hostAddress);
				} catch (InterruptedException e) {
					logger.info("Interrupted while stopping " + hostAddress);
				} catch (TimeoutException e) {
					logger.info("Stopping " + hostAddress + " times out.");
				}
			}
		});
	}
}
