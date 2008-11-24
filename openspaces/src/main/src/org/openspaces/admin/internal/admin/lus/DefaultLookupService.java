package org.openspaces.admin.internal.admin.lus;

import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.Machine;
import org.openspaces.admin.internal.admin.machine.InternalMachine;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultLookupService implements InternalLookupService {

    private ServiceRegistrar registrar;

    private ServiceID serviceID;

    private volatile TransportConfiguration transportConfiguration;

    private volatile InternalMachine machine;

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

    public void setMachine(InternalMachine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return this.machine;
    }

    public TransportConfiguration getTransportConfiguration() throws AdminException {
        if (transportConfiguration != null) {
            return transportConfiguration;
        }
        try {
            transportConfiguration = ((NIOInfoProvider) registrar.getRegistrar()).getTransportConfiguration();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport configuration", e);
        }
        return transportConfiguration;
    }

    public TransportStatistics getTransportStatistics() throws AdminException {
        try {
            return ((NIOInfoProvider) registrar.getRegistrar()).getTransportStatistics();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport statistics", e);
        }
    }
}
