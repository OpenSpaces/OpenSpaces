package org.openspaces.admin.internal.lus;

import com.gigaspaces.jvm.JVMInfoProvider;
import com.gigaspaces.jvm.VirtualMachineConfiguration;
import com.gigaspaces.jvm.VirtualMachineStatistics;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;
import com.gigaspaces.operatingsystem.OSInfoProvider;
import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultLookupService extends AbstractGridComponent implements InternalLookupService {

    private ServiceRegistrar registrar;

    private ServiceID serviceID;

    public DefaultLookupService(ServiceRegistrar registrar, ServiceID serviceID, InternalAdmin admin) {
        super(admin);
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

    public OperatingSystemConfiguration getOperatingSystemConfiguration() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getOperatingSystemConfiguration();
    }

    public OperatingSystemStatistics getOperatingSystemStatistics() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getOperatingSystemStatistics();
    }

    public VirtualMachineConfiguration getVirtualMachineConfiguration() throws RemoteException {
        return ((JVMInfoProvider) registrar.getRegistrar()).getJvmConfiguration();
    }

    public VirtualMachineStatistics getVirtualMachineStatistics() throws RemoteException {
        return ((JVMInfoProvider) registrar.getRegistrar()).getJvmStatistics();
    }
}
