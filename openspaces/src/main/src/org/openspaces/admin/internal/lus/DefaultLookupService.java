package org.openspaces.admin.internal.lus;

import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultLookupService extends AbstractGridComponent implements InternalLookupService {

    private ServiceRegistrar registrar;

    private ServiceID serviceID;

    public DefaultLookupService(ServiceRegistrar registrar, ServiceID serviceID) {
        this.registrar = registrar;
        this.serviceID = serviceID;
    }

    public String getUID() {
        return getServiceID().toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public ServiceRegistrar getRegistrar() {
        return this.registrar;
    }

    public TransportConfiguration getTransportConfiguration() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getTransportConfiguration();
    }

    public TransportStatistics getTransportStatistics() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getTransportStatistics();
    }
}
