package org.openspaces.admin.internal.gsc;

import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainer extends AbstractGridComponent implements InternalGridServiceContainer {

    private final ServiceID serviceID;

    private final GSC gsc;

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

    public TransportConfiguration getTransportConfiguration() throws RemoteException {
        return gsc.getTransportConfiguration();
    }

    public TransportStatistics getTransportStatistics() throws RemoteException {
        return gsc.getTransportStatistics();
    }

    public OperatingSystemConfiguration getOperatingSystemConfiguration() throws RemoteException {
        return gsc.getOperatingSystemConfiguration();
    }

    public OperatingSystemStatistics getOperatingSystemStatistics() throws RemoteException {
        return gsc.getOperatingSystemStatistics();
    }
}