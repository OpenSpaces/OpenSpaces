package org.openspaces.admin.internal.gsm;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.jvm.VirtualMachineConfiguration;
import com.gigaspaces.jvm.VirtualMachineStatistics;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;
import net.jini.core.lookup.ServiceID;
import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager extends AbstractGridComponent implements InternalGridServiceManager {

    private final ServiceID serviceID;

    private final GSM gsm;

    private final ProvisionMonitorAdmin admin;

    public DefaultGridServiceManager(ServiceID serviceID, GSM gsm) throws RemoteException {
        this.serviceID = serviceID;
        this.gsm = gsm;
        this.admin = (ProvisionMonitorAdmin) gsm.getAdmin();
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

    public ProvisionMonitorAdmin getAdmin() {
        return admin;
    }

    public TransportConfiguration getTransportConfiguration() throws RemoteException {
        return gsm.getTransportConfiguration();
    }

    public TransportStatistics getTransportStatistics() throws RemoteException {
        return gsm.getTransportStatistics();
    }

    public OperatingSystemConfiguration getOperatingSystemConfiguration() throws RemoteException {
        return gsm.getOperatingSystemConfiguration();
    }

    public OperatingSystemStatistics getOperatingSystemStatistics() throws RemoteException {
        return gsm.getOperatingSystemStatistics();
    }

    public VirtualMachineConfiguration getVirtualMachineConfiguration() throws RemoteException {
        return gsm.getJvmConfiguration();
    }

    public VirtualMachineStatistics getVirtualMachineStatistics() throws RemoteException {
        return gsm.getJvmStatistics();
    }
}
