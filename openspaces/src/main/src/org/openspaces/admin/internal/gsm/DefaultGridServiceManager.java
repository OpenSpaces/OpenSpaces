package org.openspaces.admin.internal.gsm;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.grid.security.Credentials;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import org.jini.rio.core.OperationalString;
import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager extends AbstractAgentGridComponent implements InternalGridServiceManager {

    private final ServiceID serviceID;

    private final GSM gsm;

    private final ProvisionMonitorAdmin gsmAdmin;

    private final Credentials credentials;

    public DefaultGridServiceManager(ServiceID serviceID, GSM gsm, InternalAdmin admin, Credentials credentials,
                                     int agentId, String agentUid) throws RemoteException {
        super(admin, agentId, agentUid);
        this.serviceID = serviceID;
        this.gsm = gsm;
        this.gsmAdmin = (ProvisionMonitorAdmin) gsm.getAdmin();
        this.credentials = credentials;
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
        ProcessingUnit processingUnit = deploy(deployment.toProcessingUnitDeployment());
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitSpaceCorrelatedEventListener correlated = new ProcessingUnitSpaceCorrelatedEventListener() {
            public void processingUnitSpaceCorrelated(ProcessingUnitSpaceCorrelatedEvent event) {
                latch.countDown();
            }
        };
        processingUnit.getSpaceCorrelated().add(correlated);
        try {
            // TODO make this configurable
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // do nothing
        } finally {
            processingUnit.getSpaceCorrelated().remove(correlated);
        }
        return processingUnit;
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to deploy a processing unit");
        }
        Deploy deploy = new Deploy();
        Deploy.setDisableInfoLogging(true);
        deploy.setGroups(getAdmin().getGroups());
        StringBuilder locatorsString = new StringBuilder();
        for (LookupLocator locator : getAdmin().getLocators()) {
            locatorsString.append(locator).append(',');
        }
        deploy.setLocators(locatorsString.toString());
        deploy.setDeployAdmin(getGSMAdmin());
        List<GSM> gsms = new ArrayList<GSM>();
        for (GridServiceManager gridServiceManager : getAdmin().getGridServiceManagers()) {
            gsms.add(((InternalGridServiceManager) gridServiceManager).getGSM());
        }
        deploy.setGSMs(gsms.toArray(new GSM[gsms.size()]));

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
            latch.await(20, TimeUnit.SECONDS);
            return ref.get();
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + deployment.getProcessingUnit() + "]", e);
        } finally {
            Deploy.setDisableInfoLogging(false);
            getAdmin().getProcessingUnits().getProcessingUnitAdded().remove(added);
        }
    }

    public void undeployProcessingUnit(String processingUnitName) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to deploy a processing unit");
        }
        try {
            getGSMAdmin().undeploy(processingUnitName);
        } catch (Exception e) {
            throw new AdminException("Failed to undeploy processing unit [" + processingUnitName + "]");
        }
    }

    public void destroyInstance(ProcessingUnitInstance processingUnitInstance) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to destroy a processing unit instance");
        }
        try {
            ((InternalProcessingUnitInstance) processingUnitInstance).getPUServiceBean().destroy();
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
            } else {
                throw new AdminException("Faield to destroy procesing unit instance", e);
            }
        }
    }

    public void decrementInstance(ProcessingUnitInstance processingUnitInstance) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to decrement a processing unit instance");
        }
        if (!processingUnitInstance.getProcessingUnit().canDecrementInstance()) {
            throw new AdminException("Processing unit does not allow to decrement instances on it");
        }
        try {
            gsm.decrement(processingUnitInstance.getProcessingUnit().getName(), ((InternalProcessingUnitInstance) processingUnitInstance).getServiceID(), true);
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
            } else {
                throw new AdminException("Faield to destroy procesing unit instance", e);
            }
        }
    }

    public void incrementInstance(ProcessingUnit processingUnit) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to increment a processing unit instance");
        }
        if (!processingUnit.canIncrementInstance()) {
            throw new AdminException("Processing unit does not allow to increment instances on it");
        }
        try {
            gsm.increment(processingUnit.getName(), null);
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
            } else {
                throw new AdminException("Faield to destroy procesing unit instance", e);
            }
        }
    }

    public void relocate(ProcessingUnitInstance processingUnitInstance, GridServiceContainer gridServiceContainer) {
        if (credentials != null && credentials != Credentials.FULL) {
            throw new AdminException("No credentials to destroy a processing unit instance");
        }
        try {
            gsm.relocate(processingUnitInstance.getProcessingUnit().getName(), ((InternalProcessingUnitInstance) processingUnitInstance).getServiceID(),
                    ((InternalGridServiceContainer) gridServiceContainer).getServiceID(), null);
        } catch (Exception e) {
            throw new AdminException("Failed to relocate processing unit instnace to grid service container", e);
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
