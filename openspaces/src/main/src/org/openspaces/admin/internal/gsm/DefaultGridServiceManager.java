package org.openspaces.admin.internal.gsm;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.lookup.ServiceID;
import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager extends AbstractGridComponent implements InternalGridServiceManager {

    private final ServiceID serviceID;

    private final GSM gsm;

    private final ProvisionMonitorAdmin gsmAdmin;

    public DefaultGridServiceManager(ServiceID serviceID, GSM gsm, InternalAdmin admin) throws RemoteException {
        super(admin);
        this.serviceID = serviceID;
        this.gsm = gsm;
        this.gsmAdmin = (ProvisionMonitorAdmin) gsm.getAdmin();
    }

    public String getUid() {
        return serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSM getGSM() {
        return this.gsm;
    }

    public ProvisionMonitorAdmin getGSMAdmin() {
        return gsmAdmin;
    }

    public NIODetails getNIODetails() throws RemoteException {
        return gsm.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsm.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsm.getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return gsm.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return gsm.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return gsm.getJVMStatistics();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGridServiceManager that = (DefaultGridServiceManager) o;
        return serviceID.equals(that.serviceID);
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }
}
