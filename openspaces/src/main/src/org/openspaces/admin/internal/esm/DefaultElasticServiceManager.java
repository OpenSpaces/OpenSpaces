package org.openspaces.admin.internal.esm;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.dump.InternalDumpResult;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.grid.esm.ESM;
import org.openspaces.pu.container.servicegrid.deploy.Deploy;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.security.SecurityException;

/**
 * @author Moran Avigdor
 */
public class DefaultElasticServiceManager extends AbstractAgentGridComponent implements InternalElasticServiceManager {

    private final ServiceID serviceID;

    private final ESM esm;

    public DefaultElasticServiceManager(ServiceID serviceID, ESM esm, InternalAdmin admin, int agentId, String agentUid)
    throws RemoteException {
        super(admin, agentId, agentUid);
        this.serviceID = serviceID;
        this.esm = esm;
    }

    public String getUid() {
        return serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }
    
    public ProcessingUnit deploy(final ElasticDataGridDeployment deployment) {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(final ElasticDataGridDeployment deployment, long timeout, TimeUnit timeUnit) {
        final AtomicReference<ProcessingUnit> ref = new AtomicReference<ProcessingUnit>();
        ref.set(getAdmin().getProcessingUnits().getProcessingUnit(deployment.getDataGridName()));
        if (ref.get() != null) {
            return ref.get();
        }

        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitAddedEventListener added = new ProcessingUnitAddedEventListener() {
            public void processingUnitAdded(ProcessingUnit processingUnit) {
                if (deployment.getDataGridName().equals(processingUnit.getName())) {
                    ref.set(processingUnit);
                    latch.countDown();
                }
            }
        };
        getAdmin().getProcessingUnits().getProcessingUnitAdded().add(added);
        try {
            esm.deploy(deployment);
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (SecurityException se) {
            throw new AdminException("No privileges to deploy an elastic data grid", se);
        } catch (ProcessingUnitAlreadyDeployedException e) {
            return getAdmin().getProcessingUnits().waitFor(deployment.getDataGridName());
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + deployment.getDataGridName() + "]", e);
        } finally {
            Deploy.setDisableInfoLogging(false);
            getAdmin().getProcessingUnits().getProcessingUnitAdded().remove(added);
        }
    }

    public LogEntries logEntries(LogEntryMatcher matcher) throws AdminException {
        if (getGridServiceAgent() != null) {
            return getGridServiceAgent().logEntries(LogProcessType.ESM, getVirtualMachine().getDetails().getPid(), matcher);
        }
        return logEntriesDirect(matcher);
    }

    public LogEntries logEntriesDirect(LogEntryMatcher matcher) throws AdminException {
        try {
            return esm.logEntriesDirect(matcher);
        } catch (IOException e) {
            throw new AdminException("Failed to get log", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        try {
            return new InternalDumpResult(this, esm, esm.generateDump(cause, context));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processors) throws AdminException {
        try {
            return new InternalDumpResult(this, esm, esm.generateDump(cause, context, processors));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    // NIO, OS, and JVM stats

    public NIODetails getNIODetails() throws RemoteException {
        return esm.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return esm.getNIOStatistics();
    }

    public long getCurrentTimeInMillis() throws RemoteException {
        return esm.getCurrentTimestamp();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return esm.getOSDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return esm.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return esm.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return esm.getJVMStatistics();
    }

    public void runGc() throws RemoteException {
        esm.runGc();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultElasticServiceManager that = (DefaultElasticServiceManager) o;
        return serviceID.equals(that.serviceID);
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }
}
