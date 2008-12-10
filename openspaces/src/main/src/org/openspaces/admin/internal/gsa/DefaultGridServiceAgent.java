package org.openspaces.admin.internal.gsa;

import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.grid.gsa.GSProcessOptions;
import com.gigaspaces.grid.security.Credentials;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgent extends AbstractGridComponent implements InternalGridServiceAgent {

    private final ServiceID serviceID;

    private final GSA gsa;

    private final Credentials credentials;

    public DefaultGridServiceAgent(ServiceID serviceID, GSA gsa, InternalAdmin admin, Credentials credentials) {
        super(admin);
        this.serviceID = serviceID;
        this.gsa = gsa;
        this.credentials = credentials;
    }

    public String getUid() {
        return this.serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSA getGSA() {
        return this.gsa;
    }

    public void startGridServiceManager() {
        try {
            gsa.startProcess(new GSProcessOptions(GSProcessOptions.Type.GSM).setUseScript(true));
        } catch (IOException e) {
            throw new AdminException("Failed to start GSM", e);
        }
    }

    public void startGridServiceContainer() {
        try {
            gsa.startProcess(new GSProcessOptions(GSProcessOptions.Type.GSC).setUseScript(true));
        } catch (IOException e) {
            throw new AdminException("Failed to start GSC", e);
        }
    }

    public void startLookupService() {
        try {
            gsa.startProcess(new GSProcessOptions(GSProcessOptions.Type.LUS).setUseScript(true));
        } catch (IOException e) {
            throw new AdminException("Failed to start LUS", e);
        }
    }

    public NIODetails getNIODetails() throws RemoteException {
        return gsa.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsa.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsa.getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return gsa.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return gsa.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return gsa.getJVMStatistics();
    }
}
