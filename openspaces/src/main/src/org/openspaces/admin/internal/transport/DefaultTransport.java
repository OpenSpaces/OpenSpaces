package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIODetails;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportStatistics;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultTransport implements InternalTransport {

    private final String uid;

    private final TransportDetails transportDetails;

    private final Set<InternalTransportInfoProvider> transportInfoProviders = new ConcurrentHashSet<InternalTransportInfoProvider>();

    public DefaultTransport(NIODetails nioDetails) {
        this.transportDetails = new DefaultTransportDetails(nioDetails);
        this.uid = getHost() + ":" + getPort();
    }

    public void addTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider) {
        transportInfoProviders.add(transportInfoProvider);
    }

    public void removeTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider) {
        transportInfoProviders.remove(transportInfoProvider);
    }

    public boolean hasTransportInfoProviders() {
        return !transportInfoProviders.isEmpty();
    }

    public String getUID() {
        return this.uid;
    }

    public String getHost() {
        return transportDetails.getHost();
    }

    public int getPort() {
        return transportDetails.getPort();
    }

    public TransportDetails getDetails() {
        return transportDetails;
    }

    private static final TransportStatistics NA_TRANSPORT_STATS = new DefaultTransportStatistics();

    public TransportStatistics getStatistics() {
        for (InternalTransportInfoProvider provider : transportInfoProviders) {
            try {
                return new DefaultTransportStatistics(provider.getNIOStatistics());
            } catch (RemoteException e) {
                // failed to get it, try next one
            }
        }
        // return an NA if fails
        return NA_TRANSPORT_STATS;
    }
}
