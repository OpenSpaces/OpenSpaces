package org.openspaces.admin.internal.admin.gsm;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.Machine;
import org.openspaces.admin.internal.admin.machine.InternalMachine;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager implements InternalGridServiceManager {

    private final ServiceID serviceID;

    private final GSM gsm;

    private volatile TransportConfiguration transportConfiguration;

    private volatile InternalMachine machine;

    public DefaultGridServiceManager(ServiceID serviceID, GSM gsm) {
        this.serviceID = serviceID;
        this.gsm = gsm;
    }

    public String getUID() {
        return serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSM getGSM() {
        return this.gsm;
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
            transportConfiguration = gsm.getTransportConfiguration();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport configuration", e);
        }
        return transportConfiguration;
    }

    public TransportStatistics getTransportStatistics() throws AdminException {
        try {
            return gsm.getTransportStatistics();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport statistics", e);
        }
    }
}
