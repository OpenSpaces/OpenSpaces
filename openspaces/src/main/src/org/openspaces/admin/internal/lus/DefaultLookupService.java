package org.openspaces.admin.internal.lus;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMInfoProvider;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.log.InternalLogHelper;
import com.gigaspaces.internal.log.InternalLogProvider;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSInfoProvider;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.internal.dump.InternalDumpProvider;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.internal.dump.InternalDumpResult;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

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

    public LogEntries logEntries(LogEntryMatcher matcher) throws AdminException {
        if (getGridServiceAgent() != null) {
            return getGridServiceAgent().logEntries(LogProcessType.LUS, getVirtualMachine().getDetails().getPid(), matcher);
        }
        return logEntriesDirect(matcher);
    }

    public LogEntries logEntriesDirect(LogEntryMatcher matcher) throws AdminException {
        try {
            return InternalLogHelper.clientSideProcess(matcher, ((InternalLogProvider) registrar.getRegistrar()).logEntriesDirect(matcher));
        } catch (IOException e) {
            throw new AdminException("Failed to get log", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        try {
            return new InternalDumpResult(this, ((InternalDumpProvider) registrar.getRegistrar()), ((InternalDumpProvider) registrar.getRegistrar()).generateDump(cause, context));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processors) throws AdminException {
        try {
            return new InternalDumpResult(this, ((InternalDumpProvider) registrar.getRegistrar()), ((InternalDumpProvider) registrar.getRegistrar()).generateDump(cause, context, processors));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    public NIODetails getNIODetails() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getNIOStatistics();
    }

    public long getCurrentTimeInMillis() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getCurrentTimestamp();
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

    public boolean isRunning() {
        return admin.getLookupServices().getLookupServiceByUID(getUid()) != null;
    }
}
