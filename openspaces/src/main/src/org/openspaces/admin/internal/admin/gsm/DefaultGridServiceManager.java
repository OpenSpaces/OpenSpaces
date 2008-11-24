package org.openspaces.admin.internal.admin.gsm;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.internal.admin.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager extends AbstractGridComponent implements InternalGridServiceManager {

    private final ServiceID serviceID;

    private final GSM gsm;

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

    public TransportConfiguration getTransportConfiguration() throws RemoteException {
        return gsm.getTransportConfiguration();
    }

    public TransportStatistics getTransportStatistics() throws RemoteException {
        return gsm.getTransportStatistics();
    }
}
