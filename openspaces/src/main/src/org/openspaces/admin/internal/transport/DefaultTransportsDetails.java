package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportsDetails;

/**
 * @author kimchy
 */
public class DefaultTransportsDetails implements TransportsDetails {

    private final TransportDetails[] details;

    public DefaultTransportsDetails(TransportDetails[] details) {
        this.details = details;
    }

    public int getMinThreads() {
        int total = 0;
        for (TransportDetails detail : details) {
            total += detail.getMinThreads();
        }
        return total;
    }

    public int getMaxThreads() {
        int total = 0;
        for (TransportDetails detail : details) {
            total += detail.getMaxThreads();
        }
        return total;
    }
}
