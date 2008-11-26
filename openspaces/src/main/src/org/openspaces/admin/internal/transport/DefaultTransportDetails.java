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

    public String getLocalHostAddress() {
        if (nioDetails.getLocalHostAddress().length() == 0) {
            return nioDetails.getHost();
        }
        return nioDetails.getLocalHostAddress();
    }

    public String getLocalHostName() {
        if (nioDetails.getLocalHostName().length() == 0) {
            return nioDetails.getHost();
        }
        return nioDetails.getLocalHostName();
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
