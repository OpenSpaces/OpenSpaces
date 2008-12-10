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
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to start a GSM");
        }
        try {
            gsa.startProcess(new GSProcessOptions(GSProcessOptions.Type.GSM).setUseScript(true));
        } catch (IOException e) {
            throw new AdminException("Failed to start GSM", e);
        }
    }

    public GridServiceManager startGridServiceManagerAndWait() {
        return startGridServiceManagerAndWait(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public GridServiceManager startGridServiceManagerAndWait(long timeout, TimeUnit timeUnit) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to start a GSM");
        }
        final Object monitor = new Object();
        final AtomicReference<GridServiceManager> ref = new AtomicReference<GridServiceManager>();
        GridServiceManagerAddedEventListener added = new GridServiceManagerAddedEventListener() {
            public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
                String agentUid = ((InternalGridServiceManager) gridServiceManager).getAgentUid();
                if (agentUid != null && agentUid.equals(getUid())) {
                    ref.set(gridServiceManager);
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                }
            }
        };
        // adding now, so we get all the events for existing ones
        getAdmin().getGridServiceManagers().getGridServiceManagerAdded().add(added);
        // reset the refernece
        ref.set(null);
        try {
            startGridServiceManager();
            synchronized (monitor) {
                monitor.wait(timeUnit.toMillis(timeout));
            }
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getAdmin().getGridServiceManagers().getGridServiceManagerAdded().remove(added);
        }
    }

    public void startGridServiceContainer() {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to start a GSC");
        }
        try {
            gsa.startProcess(new GSProcessOptions(GSProcessOptions.Type.GSC).setUseScript(true));
        } catch (IOException e) {
            throw new AdminException("Failed to start GSC", e);
        }
    }

    public GridServiceContainer startGridServiceContainerAndWait() {
        return startGridServiceContainerAndWait(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public GridServiceContainer startGridServiceContainerAndWait(long timeout, TimeUnit timeUnit) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to start a GSM");
        }
        final Object monitor = new Object();
        final AtomicReference<GridServiceContainer> ref = new AtomicReference<GridServiceContainer>();
        GridServiceContainerAddedEventListener added = new GridServiceContainerAddedEventListener() {

            public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
                String agentUid = ((InternalGridServiceContainer) gridServiceContainer).getAgentUid();
                if (agentUid != null && agentUid.equals(getUid())) {
                    ref.set(gridServiceContainer);
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                }
            }
        };
        // adding now, so we get all the events for existing ones
        getAdmin().getGridServiceContainers().getGridServiceContainerAdded().add(added);
        // reset the refernece
        ref.set(null);
        try {
            startGridServiceContainer();
            synchronized (monitor) {
                monitor.wait(timeUnit.toMillis(timeout));
            }
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getAdmin().getGridServiceContainers().getGridServiceContainerAdded().remove(added);
        }
    }

    public void startLookupService() {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to start a LUS");
        }
        try {
            gsa.startProcess(new GSProcessOptions(GSProcessOptions.Type.LUS).setUseScript(true));
        } catch (IOException e) {
            throw new AdminException("Failed to start LUS", e);
        }
    }

    public LookupService startLookupServiceAndWait() {
        return startLookupServiceAndWait(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public LookupService startLookupServiceAndWait(long timeout, TimeUnit timeUnit) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to start a GSM");
        }
        final Object monitor = new Object();
        final AtomicReference<LookupService> ref = new AtomicReference<LookupService>();
        LookupServiceAddedEventListener added = new LookupServiceAddedEventListener() {
            public void lookupServiceAdded(LookupService lookupService) {
                String agentUid = ((InternalLookupService) lookupService).getAgentUid();
                if (agentUid != null && agentUid.equals(getUid())) {
                    ref.set(lookupService);
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                }
            }
        };
        // adding now, so we get all the events for existing ones
        getAdmin().getLookupServices().getLookupServiceAdded().add(added);
        // reset the refernece
        ref.set(null);
        try {
            startLookupService();
            synchronized (monitor) {
                monitor.wait(timeUnit.toMillis(timeout));
            }
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getAdmin().getLookupServices().getLookupServiceAdded().remove(added);
        }
    }

    public void kill(InternalAgentGridComponent agentGridComponent) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to kill");
        }
        try {
            gsa.stopProcess(agentGridComponent.getAgentId());
        } catch (RemoteException e) {
            throw new AdminException("Failed to kill [" + agentGridComponent.getUid() + "]", e);
        }
    }

    public void restart(InternalAgentGridComponent agentGridComponent) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to restart");
        }
        try {
            gsa.restartProcess(agentGridComponent.getAgentId());
        } catch (IOException e) {
            throw new AdminException("Failed to restart [" + agentGridComponent.getUid() + "]", e);
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
