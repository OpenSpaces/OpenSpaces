package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIODetails;
import org.openspaces.admin.transport.TransportDetails;

/**
 * @author kimchy
 */
public class DefaultTransportDetails implements TransportDetails {

    private final NIODetails nioDetails;

    public DefaultTransportDetails(NIODetails nioDetails) {
        this.nioDetails = nioDetails;
    }

    public String getHost() {
        return nioDetails.getHost();
    }

    public int getPort() {
        return nioDetails.getPort();
    }

    public int getMinThreads() {
        return nioDetails.getMinThreads();
    }

    public int getMaxThreads() {
        return nioDetails.getMaxThreads();
    }
}
