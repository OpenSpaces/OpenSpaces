package org.openspaces.pu.container.jee.jetty.support;

/**
 * A no op free port generator.
 *
 * @author kimchy
 */
public class NoOpFreePortGenerator implements FreePortGenerator {

    public PortHandle nextAvailablePort(final int startFromPort, int retryCount) {
        return new PortHandle() {
            public int getPort() {
                return startFromPort;
            }

            public void release() {
                // no op
            }
        };
    }
}
