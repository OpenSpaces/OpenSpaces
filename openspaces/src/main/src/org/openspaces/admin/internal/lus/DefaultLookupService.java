package org.openspaces.admin.internal.lus;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMInfoProvider;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSInfoProvider;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultLookupService extends AbstractAgentGridComponent implements InternalLookupService {

    private final ServiceRegistrar registrar;

    private final ServiceID serviceID;

    public DefaultLookupService(ServiceRegistrar registrar, ServiceID serviceID, InternalAdmin admin,
                                int agentId, String agentUid) {
        super(admin, agentId, agentUid);
        this.registrar = registrar;
        this.serviceID = serviceID;
    }

    public String getUid() {
        return getServiceID().toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public String[] getLookupGroups() {
        try {
            return registrar.getGroups();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get groups", e);
        }
    }

    public LookupLocator getLookupLocator() {
        try {
            return registrar.getLocator();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get locator", e);
        }
    }

    public ServiceRegistrar getRegistrar() {
        return this.registrar;
    }

    public NIODetails getNIODetails() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getOSDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return ((JVMInfoProvider) registrar.getRegistrar()).getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return ((JVMInfoProvider) registrar.getRegistrar()).getJVMStatistics();
    }

    public void runGc() throws RemoteException {
        ((JVMInfoProvider) registrar.getRegistrar()).runGc();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultLookupService that = (DefaultLookupService) o;
        return serviceID.equals(that.serviceID);
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }
}
