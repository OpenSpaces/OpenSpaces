package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import org.openspaces.admin.AdminException;
import org.openspaces.core.util.ConcurrentHashSet;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultTransport implements InternalTransport {

    private final String uid;

    private final TransportConfiguration config;

    private final Set<InternalTransportInfoProvider> transportInfoProviders = new ConcurrentHashSet<InternalTransportInfoProvider>();

    public DefaultTransport(TransportConfiguration config) {
        this.config = config;
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
        return config.getHost();
    }

    public int getPort() {
        return config.getPort();
    }

    public TransportConfiguration getConfiguration() {
        return config;
    }

    public TransportStatistics getStatistics() {
        Iterator<InternalTransportInfoProvider> iter = transportInfoProviders.iterator();
        if (!iter.hasNext()) {
            throw new IllegalStateException("No transport information provider is bounded to transport with host [" + getHost() + "] and port [" + getPort() + "]");
        }
        try {
            return iter.next().getTransportStatistics();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport statistics for host [" + getHost() + "] and port [" + getPort() + "]", e);
        }
    }
}
