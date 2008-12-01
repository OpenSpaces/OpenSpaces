package org.openspaces.admin.internal.transport;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.transport.Transport;
import org.openspaces.core.util.ConcurrentHashSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultTransports implements InternalTransports {


    private final Map<String, Transport> transportsByUID = new SizeConcurrentHashMap<String, Transport>();

    private final Map<String, Set<Transport>> transportsByHost = new SizeConcurrentHashMap<String, Set<Transport>>();

    public Transport[] getTransports() {
        return transportsByUID.values().toArray(new Transport[0]);
    }

    public Iterator<Transport> iterator() {
        return transportsByUID.values().iterator();
    }

    public Transport[] getTransports(String host) {
        Set<Transport> transportByHost = transportsByHost.get(host);
        if (transportByHost == null) {
            return new Transport[0];
        }
        return transportByHost.toArray(new Transport[0]);
    }

    public Transport getTransportByHostAndPort(String host, int port) {
        return transportsByUID.get(host + ":" + port);
    }

    public Transport getTransportByUID(String uid) {
        return transportsByUID.get(uid);
    }

    public int size() {
        return transportsByUID.size();
    }

    public void addTransport(Transport transport) {
        transportsByUID.put(transport.getUid(), transport);
        Set<Transport> transportByHost = transportsByHost.get(transport.getHost());
        if (transportByHost == null) {
            synchronized (transportsByHost) {
                transportByHost = transportsByHost.get(transport.getHost());
                if (transportByHost == null) {
                    transportByHost = new ConcurrentHashSet<Transport>();
                }
            }
        }
        transportByHost.add(transport);
    }

    public void removeTransport(String uid) {
        Transport transport = transportsByUID.remove(uid);
        if (transport == null) {
            return;
        }
        Set<Transport> transportByHost = transportsByHost.get(transport.getHost());
        if (transportByHost == null) {
            synchronized (transportsByHost) {
                transportByHost = transportsByHost.get(transport.getHost());
                if (transportByHost != null) {
                    transportByHost.remove(transport);
                }
            }
        } else {
            transportByHost.remove(transport);
        }
    }
}
