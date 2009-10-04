package org.openspaces.admin.internal.gsa;

import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.log.LogEntry;
import com.gigaspaces.log.LogEntryMatcher;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsa.GridServiceManagerOptions;
import org.openspaces.admin.gsa.GridServiceOptions;
import org.openspaces.admin.gsa.LookupServiceOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.support.AbstractGridComponent;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgent extends AbstractGridComponent implements InternalGridServiceAgent {

    private final ServiceID serviceID;

    private final GSA gsa;

    private volatile AgentProcessesDetails processesDetails;

    public DefaultGridServiceAgent(ServiceID serviceID, GSA gsa, InternalAdmin admin, AgentProcessesDetails processesDetails) {
        super(admin);
        this.serviceID = serviceID;
        this.gsa = gsa;
        this.processesDetails = processesDetails;
    }

    public String getUid() {
        return this.serviceID.toString();
    }

    public AgentProcessesDetails getProcessesDetails() {
        return processesDetails;
    }

    public void setProcessesDetails(AgentProcessesDetails processesDetails) {
        this.processesDetails = processesDetails;
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSA getGSA() {
        return this.gsa;
    }

    public void startGridService(GridServiceOptions options) {
        try {
            gsa.startProcess(options.getOptions());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to start a service", se);
        } catch (IOException e) {
            throw new AdminException("Failed to start Grid Service", e);
        }
    }

    public void startGridService(GridServiceManagerOptions options) {
        internalStartGridService(options);
    }

    private int internalStartGridService(GridServiceManagerOptions options) {
        try {
            return gsa.startProcess(options.getOptions());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to start a GSM", se);
        } catch (IOException e) {
            throw new AdminException("Failed to start GSM", e);
        }
    }

    public GridServiceManager startGridServiceAndWait(GridServiceManagerOptions options) {
        return startGridServiceAndWait(options, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public GridServiceManager startGridServiceAndWait(GridServiceManagerOptions options, long timeout, TimeUnit timeUnit) {
        final int agentId = internalStartGridService(options);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceManager> ref = new AtomicReference<GridServiceManager>();
        GridServiceManagerAddedEventListener added = new GridServiceManagerAddedEventListener() {
            public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
                String agentUid = ((InternalGridServiceManager) gridServiceManager).getAgentUid();
                if (agentUid != null && agentUid.equals(getUid())) {
                    if (agentId == gridServiceManager.getAgentId()) {
                        ref.set(gridServiceManager);
                        latch.countDown();
                    }
                }
            }
        };
        // adding now, so we get all the events for existing ones
        getAdmin().getGridServiceManagers().getGridServiceManagerAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getAdmin().getGridServiceManagers().getGridServiceManagerAdded().remove(added);
        }
    }

    public void startGridService(GridServiceContainerOptions options) {
        internalStartGridService(options);
    }

    public int internalStartGridService(GridServiceContainerOptions options) {
        try {
            return gsa.startProcess(options.getOptions());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to start a GSC", se);
        } catch (IOException e) {
            throw new AdminException("Failed to start GSC", e);
        }
    }

    public GridServiceContainer startGridServiceAndWait(GridServiceContainerOptions options) {
        return startGridServiceAndWait(options, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public GridServiceContainer startGridServiceAndWait(GridServiceContainerOptions options, long timeout, TimeUnit timeUnit) {
        final int agentId = internalStartGridService(options);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceContainer> ref = new AtomicReference<GridServiceContainer>();
        GridServiceContainerAddedEventListener added = new GridServiceContainerAddedEventListener() {

            public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
                String agentUid = ((InternalGridServiceContainer) gridServiceContainer).getAgentUid();
                if (agentUid != null && agentUid.equals(getUid())) {
                    if (agentId == gridServiceContainer.getAgentId()) {
                        ref.set(gridServiceContainer);
                        latch.countDown();
                    }
                }
            }
        };
        // adding now, so we get all the events for existing ones
        getAdmin().getGridServiceContainers().getGridServiceContainerAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getAdmin().getGridServiceContainers().getGridServiceContainerAdded().remove(added);
        }
    }

    public void startGridService(LookupServiceOptions options) {
        internalStartGridService(options);
    }

    public int internalStartGridService(LookupServiceOptions options) {
        try {
            return gsa.startProcess(options.getOptions());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to start a Lookup Service", se);
        } catch (IOException e) {
            throw new AdminException("Failed to start LUS", e);
        }
    }

    public LookupService startGridServiceAndWait(LookupServiceOptions options) {
        return startGridServiceAndWait(options, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public LookupService startGridServiceAndWait(LookupServiceOptions options, long timeout, TimeUnit timeUnit) {
        final int agentId = internalStartGridService(options);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<LookupService> ref = new AtomicReference<LookupService>();
        LookupServiceAddedEventListener added = new LookupServiceAddedEventListener() {
            public void lookupServiceAdded(LookupService lookupService) {
                String agentUid = ((InternalLookupService) lookupService).getAgentUid();
                if (agentUid != null && agentUid.equals(getUid())) {
                    if (agentId == lookupService.getAgentId()) {
                        ref.set(lookupService);
                        latch.countDown();
                    }
                }
            }
        };
        // adding now, so we get all the events for existing ones
        getAdmin().getLookupServices().getLookupServiceAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getAdmin().getLookupServices().getLookupServiceAdded().remove(added);
        }
    }

    public void kill(InternalAgentGridComponent agentGridComponent) {
        try {
            gsa.killProcess(agentGridComponent.getAgentId());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to kill", se);
        } catch (RemoteException e) {
            throw new AdminException("Failed to kill [" + agentGridComponent.getUid() + "]", e);
        }
    }

    public void restart(InternalAgentGridComponent agentGridComponent) {
        try {
            gsa.restartProcess(agentGridComponent.getAgentId());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to restart", se);
        } catch (IOException e) {
            throw new AdminException("Failed to restart [" + agentGridComponent.getUid() + "]", e);
        }
    }

    public LogEntry[] log(LogEntryMatcher matcher) throws AdminException {
        try {
            return gsa.log(matcher);
        } catch (IOException e) {
            throw new AdminException("Failed to get log", e);
        }
    }

    public NIODetails getNIODetails() throws RemoteException {
        return gsa.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsa.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsa.getOSDetails();
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

    public void runGc() throws RemoteException {
        gsa.runGc();
    }
}
