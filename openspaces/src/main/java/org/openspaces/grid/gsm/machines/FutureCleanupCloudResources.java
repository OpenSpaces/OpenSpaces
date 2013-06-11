package org.openspaces.grid.gsm.machines;

import org.openspaces.core.PollingFuture;

public interface FutureCleanupCloudResources extends PollingFuture<Void> {

	/**
	 * @return The Piggybacked boolean field on this future object.
	 */
	boolean isMarked();

	/**
	 * set the Piggybacked boolean field on this future object.
	 */
	void mark();
}
