package org.openspaces.admin.internal.admin;

import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.Machine;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainer implements InternalGridServiceContainer {

    private final ServiceID serviceID;

    private final GSC gsc;

    private volatile TransportConfiguration transportConfiguration;

    private volatile InternalMachine machine;

    public DefaultGridServiceContainer(ServiceID serviceID, GSC gsc) {
        this.serviceID = serviceID;
        this.gsc = gsc;
    }

    public String getUID() {
        return serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSC getGSC() {
        return this.gsc;
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
            transportConfiguration = gsc.getTransportConfiguration();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport configuration", e);
        }
        return transportConfiguration;
    }

    public TransportStatistics getTransportStatistics() throws AdminException {
        try {
            return gsc.getTransportStatistics();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport statistics", e);
        }
    }
}