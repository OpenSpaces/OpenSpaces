package org.openspaces.admin.internal.gsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;

import org.jini.rio.core.OperationalString;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.jini.rio.resources.servicecore.ServiceAdmin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.dump.InternalDumpResult;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.ElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.space.elastic.ElasticSpaceDeployment;
import org.openspaces.pu.container.servicegrid.deploy.Deploy;

import com.gigaspaces.grid.gsm.GSM;
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
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit) {
        return deploy(deployment.toProcessingUnitDeployment(), timeout, timeUnit);
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment) {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(MemcachedDeployment deployment) {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit) {
        return deploy(deployment.toProcessingUnitDeployment(), timeout, timeUnit);
    }

    public ProcessingUnit deploy(ElasticSpaceDeployment deployment) throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.toProcessingUnitDeployment());
    }

    public ProcessingUnit deploy(ElasticSpaceDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.toProcessingUnitDeployment(),timeout,timeUnit);
    }

    public ProcessingUnit deploy(ElasticProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.toProcessingUnitDeployment(),timeout,timeUnit);
    }

    public ProcessingUnit deploy(ElasticProcessingUnitDeployment deployment)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.toProcessingUnitDeployment());
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
        deploy.initializeDiscovery(gsm);
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

        try {
            if (getGSMAdmin().hasDeployed(operationalString.getName())) {
                throw new ProcessingUnitAlreadyDeployedException(operationalString.getName());
            }
        } catch (Exception e) {
            throw new AdminException("Failed to check if processing unit [" + operationalString.getName() + "] is deployed", e);
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

    public void undeploy(String processingUnitName) {
        undeployProcessingUnit(processingUnitName);
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

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        try {
            return new InternalDumpResult(this, gsm, gsm.generateDump(cause, context));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processors) throws AdminException {
        try {
            return new InternalDumpResult(this, gsm, gsm.generateDump(cause, context, processors));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    // NIO, OS, and JVM stats

    public NIODetails getNIODetails() throws RemoteException {
        return gsm.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsm.getNIOStatistics();
    }

    public long getCurrentTimeInMillis() throws RemoteException {
        return gsm.getCurrentTimestamp();
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

    public String[] listDeployDir() {

        List<String> result = new ArrayList<String>();

        try{
            URL listPU = new URL( new URL(getCodebase( gsmAdmin )), "list-pu" );
            BufferedReader reader = 
                new BufferedReader( new InputStreamReader( listPU.openStream() ) );

            String line;

            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                String puName = tokenizer.nextToken();
                result.add( puName );
            }
        }
        catch( IOException io ){
            throw new AdminException( "Failed to retrive processing units available " +
            		                    "under [GS ROOT]/deploy directory", io );
        }

        return result.toArray( new String[ 0 ]  );
    }
    
    private String getCodebase(DeployAdmin deployAdmin) throws MalformedURLException, RemoteException {
        URL url = ((ServiceAdmin) deployAdmin).getServiceElement().getExportURLs()[0];
        return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/";
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
    
    public boolean isDeployed(String processingUnitName) {

        try{
            return gsmAdmin.hasDeployed( processingUnitName );
        }
        catch (Exception e) {
            throw new AdminException( "Failed to check if processing unit [" + 
                    processingUnitName + "] deployed", e);
        }
    }
}
