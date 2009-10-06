package org.openspaces.admin.internal.gsm;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogProcessType;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import org.jini.rio.core.OperationalString;
import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventListener;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.pu.container.servicegrid.deploy.Deploy;

import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.io.IOException;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager extends AbstractAgentGridComponent implements InternalGridServiceManager {

    private final ServiceID serviceID;

    private final GSM gsm;

    private final ProvisionMonitorAdmin gsmAdmin;

    public DefaultGridServiceManager(ServiceID serviceID, GSM gsm, InternalAdmin admin, int agentId, String agentUid)
    throws RemoteException {
        super(admin, agentId, agentUid);
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

    public ProcessingUnit deploy(SpaceDeployment deployment) {
        return deploy(deployment, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit) {
        ProcessingUnit processingUnit = deploy(deployment.toProcessingUnitDeployment(), timeout, timeUnit);
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitSpaceCorrelatedEventListener correlated = new ProcessingUnitSpaceCorrelatedEventListener() {
            public void processingUnitSpaceCorrelated(ProcessingUnitSpaceCorrelatedEvent event) {
                latch.countDown();
            }
        };
        processingUnit.getSpaceCorrelated().add(correlated);
        try {
            latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            // do nothing
        } finally {
            processingUnit.getSpaceCorrelated().remove(correlated);
        }
        return processingUnit;
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment) {
        return deploy(deployment, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) {
        Deploy deploy = new Deploy();
        Deploy.setDisableInfoLogging(true);
        deploy.setGroups(getAdmin().getGroups());
        StringBuilder locatorsString = new StringBuilder();
        for (LookupLocator locator : getAdmin().getLocators()) {
            locatorsString.append(locator).append(',');
        }
        deploy.setLocators(locatorsString.toString());
        deploy.initializeDiscovery(gsm, getGSMAdmin());
        if (deployment.isSecured() != null) {
            deploy.setSecured(deployment.isSecured());
        }
        deploy.setUserDetails(deployment.getUserDetails());

        final OperationalString operationalString;
        try {
            operationalString = deploy.buildOperationalString(deployment.getDeploymentOptions());
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + deployment.getProcessingUnit() + "]", e);
        }
        final AtomicReference<ProcessingUnit> ref = new AtomicReference<ProcessingUnit>();
        ref.set(getAdmin().getProcessingUnits().getProcessingUnit(operationalString.getName()));
        if (ref.get() != null) {
            return ref.get();
        }

        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitAddedEventListener added = new ProcessingUnitAddedEventListener() {
            public void processingUnitAdded(ProcessingUnit processingUnit) {
                if (operationalString.getName().equals(processingUnit.getName())) {
                    ref.set(processingUnit);
                    latch.countDown();
                }
            }
        };
        getAdmin().getProcessingUnits().getProcessingUnitAdded().add(added);
        try {
            getGSMAdmin().deploy(operationalString);
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (SecurityException se) {
            throw new AdminException("No privileges to deploy a processing unit", se);
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + deployment.getProcessingUnit() + "]", e);
        } finally {
            Deploy.setDisableInfoLogging(false);
            getAdmin().getProcessingUnits().getProcessingUnitAdded().remove(added);
        }
    }

    public void undeployProcessingUnit(String processingUnitName) {
        try {
            getGSMAdmin().undeploy(processingUnitName);
        } catch (SecurityException se) {
            throw new AdminException("No privileges to undeploy a processing unit", se);
        } catch (Exception e) {
            throw new AdminException("Failed to undeploy processing unit [" + processingUnitName + "]", e);
        }
    }

    public void destroyInstance(ProcessingUnitInstance processingUnitInstance) {
        try {
            gsm.destroy(processingUnitInstance.getProcessingUnit().getName(), ((InternalProcessingUnitInstance) processingUnitInstance).getServiceID());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to destroy a processing unit instance", se);
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
            } else {
                throw new AdminException("Failed to destroy processing unit instance", e);
            }
        }
    }

    public void decrementInstance(ProcessingUnitInstance processingUnitInstance) {
        if (!processingUnitInstance.getProcessingUnit().canDecrementInstance()) {
            throw new AdminException("Processing unit does not allow to decrement instances on it");
        }
        try {
            gsm.decrement(processingUnitInstance.getProcessingUnit().getName(), ((InternalProcessingUnitInstance) processingUnitInstance).getServiceID(), true);
        } catch (SecurityException se) {
            throw new AdminException("No privileges to decrement a processing unit instance", se);
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
            } else {
                throw new AdminException("Failed to destroy processing unit instance", e);
            }
        }
    }

    public void incrementInstance(ProcessingUnit processingUnit) {
        if (!processingUnit.canIncrementInstance()) {
            throw new AdminException("Processing unit does not allow to increment instances on it");
        }
        try {
            gsm.increment(processingUnit.getName(), null);
        } catch (SecurityException se) {
            throw new AdminException("No privileges to increment a processing unit instance", se);
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
            } else {
                throw new AdminException("Failed to destroy processing unit instance", e);
            }
        }
    }

    /**
     * @param processingUnitInstance The processing unit instance to relocate
     * @param gridServiceContainer   The GSC to relocate to, or <code>null</code> if the GSM should decide on a
     *                               suitable GSC to relocate to.
     */
    public void relocate(ProcessingUnitInstance processingUnitInstance, GridServiceContainer gridServiceContainer) {
        try {
            gsm.relocate(
                    processingUnitInstance.getProcessingUnit().getName(),
                    ((InternalProcessingUnitInstance) processingUnitInstance).getServiceID(),
                    (gridServiceContainer == null ? null : ((InternalGridServiceContainer) gridServiceContainer).getServiceID()),
                    null);
        } catch (SecurityException se) {
            throw new AdminException("No privileges to relocate a processing unit instance", se);
        } catch (Exception e) {
            throw new AdminException("Failed to relocate processing unit instance to grid service container", e);
        }
    }

    public LogEntries logEntries(LogEntryMatcher matcher) throws AdminException {
        if (getGridServiceAgent() != null) {
            return getGridServiceAgent().logEntries(LogProcessType.GSM, getVirtualMachine().getDetails().getPid(), matcher);
        }
        return logEntriesDirect(matcher);
    }

    public LogEntries logEntriesDirect(LogEntryMatcher matcher) throws AdminException {
        try {
            return gsm.logEntriesDirect(matcher);
        } catch (IOException e) {
            throw new AdminException("Failed to get log", e);
        }
    }

    // NIO, OS, and JVM stats

    public NIODetails getNIODetails() throws RemoteException {
        return gsm.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsm.getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsm.getOSDetails();
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

    public void runGc() throws RemoteException {
        gsm.runGc();
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
