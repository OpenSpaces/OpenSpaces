package org.openspaces.pu.container.jee.jetty.support;

/**
 * A free port generator.
 *
 * @author kimchy
 */
public interface FreePortGenerator {

    /**
     * Generate the next available port from the start port and for retry count.
     */
    PortHandle nextAvailablePort(int startFromPort, int retryCount);

    /**
     * A handle to release a locked port and get the obtained port.
     */
    static interface PortHandle {
        /**
         * Returns the free port.
         */
        int getPort();

        /**
         * Releases the port lock.
         */
        void release();
    }
}
