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

    public String getBindHost() {
        return nioDetails.getBindHost();
    }

    public String getHostAddress() {
        if (nioDetails.getHostAddress().length() == 0) {
            return nioDetails.getBindHost();
        }
        return nioDetails.getHostAddress();
    }

    public String getHostName() {
        if (nioDetails.getHostName().length() == 0) {
            return nioDetails.getBindHost();
        }
        return nioDetails.getHostName();
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

    public boolean isSslEnabled() {
        return nioDetails.isSslEnabled();
    }
}
