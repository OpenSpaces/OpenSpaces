package org.openspaces.admin.internal.gsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.ApplicationAlreadyDeployedException;
import org.openspaces.admin.application.ApplicationDeployment;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.dump.InternalDumpResult;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;
import org.openspaces.admin.space.ElasticSpaceDeployment;
import org.openspaces.admin.space.SpaceDeployment;
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
        return deploy(deployment.toProcessingUnitDeployment(admin), timeout, timeUnit);
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

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) {
        String applicationName = null;
        return deploy(deployment, applicationName, timeout, timeUnit);
    }
    
    private ProcessingUnit deploy(ProcessingUnitDeployment deployment, String applicationName, long timeout, TimeUnit timeUnit) {
        
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        
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
        deploy.setApplicationName(applicationName);
        final OperationalString operationalString;
        try {
            operationalString = deploy.buildOperationalString(deployment.getDeploymentOptions());
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + deployment.getProcessingUnit() + "]", e);
        }

        boolean alreadyDeployed = false;
        try {
            alreadyDeployed = getGSMAdmin().hasDeployed(operationalString.getName());
        }
        catch (Exception e) {
            throw new AdminException("Failed to check if processing unit [" + operationalString.getName() + "] is deployed", e);
        }
        
        if (alreadyDeployed) {
            throw new ProcessingUnitAlreadyDeployedException(operationalString.getName());
        } 

        final AtomicReference<ProcessingUnit> ref = new AtomicReference<ProcessingUnit>();
        
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
        ProcessingUnit pu = null;
        try {
            getGSMAdmin().deploy(operationalString);
            latch.await(timeout, timeUnit);
            pu = ref.get();
        } catch (SecurityException se) {
            throw new AdminException("No privileges to deploy a processing unit", se);
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + deployment.getProcessingUnit() + "]", e);
        } finally {
            Deploy.setDisableInfoLogging(false);
            getAdmin().getProcessingUnits().getProcessingUnitAdded().remove(added);
        }
        
        if (!deployment.getElasticProperties().isEmpty()) {
            // wait until elastic scale strategy is being enforced
            while (System.currentTimeMillis() < end) {
                InternalGridServiceManager gridServiceManager = (InternalGridServiceManager)pu.getManagingGridServiceManager();
                if (gridServiceManager != null && gridServiceManager.isManagedByElasticServiceManager(pu)) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        return pu;
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
    
    @Override
    public boolean decrementPlannedInstances(ProcessingUnit processingUnit) {
        if (!processingUnit.canDecrementInstance()) {
            throw new AdminException("Processing unit does not allow to decrement instances on it");
        }
        try {
            return gsm.decrementPlannedIfPending(processingUnit.getName());
        } catch (SecurityException se) {
            throw new AdminException("No privileges to decrement a processing unit instance", se);
        } catch (Exception e) {
            if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                // all is well
                return true;
            } else {
                throw new AdminException("Failed to decrement processing unit instance", e);
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
                throw new AdminException("Failed to increment processing unit instance", e);
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

    public ProcessingUnit deploy(ElasticSpaceDeployment deployment) throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.toProcessingUnitDeployment(admin));
    }

    public ProcessingUnit deploy(ElasticSpaceDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.toProcessingUnitDeployment(admin),timeout,timeUnit);
    }

    public ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment,admin.getDefaultTimeout(),admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        
        return deploy(deployment.toProcessingUnitDeployment(admin),timeout,timeUnit);
    }

    public ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment)
        throws ProcessingUnitAlreadyDeployedException {
    
        return deploy(deployment,admin.getDefaultTimeout(),admin.getDefaultTimeoutTimeUnit());
    }
    
    public ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
        throws ProcessingUnitAlreadyDeployedException {

        return deploy(deployment.toProcessingUnitDeployment(admin),timeout,timeUnit);
    }
    
    public void setProcessingUnitElasticProperties(ProcessingUnit pu, Map<String,String> properties) {
        getElasticServiceManager().setProcessingUnitElasticProperties(pu, properties);
    }
    
    public void setProcessingUnitScaleStrategyConfig(ProcessingUnit pu, ScaleStrategyConfig scaleStrategyConfig) {
        getElasticServiceManager().setProcessingUnitScaleStrategyConfig(pu, scaleStrategyConfig);
    }
        
    public void updateProcessingUnitElasticPropertiesOnGsm(ProcessingUnit pu, Map<String, String> elasticProperties) {
        try {
            gsm.updateElasticProperties(pu.getName(), elasticProperties);
        } catch (Exception e) {
            throw new AdminException("Failed to update processing unit [" + pu.getName() + "] elastic properties state at the gsm", e);
        }
        
    }

    private InternalElasticServiceManager getElasticServiceManager() {
        if (admin.getElasticServiceManagers().getSize() != 1) {
            throw new AdminException("ElasticScaleHandler requires exactly one ESM server running.");
        }
        final InternalElasticServiceManager esm = (InternalElasticServiceManager) admin.getElasticServiceManagers().getManagers()[0];
        return esm;
    }

    public ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(ProcessingUnit pu) {
        if (admin.getElasticServiceManagers().isEmpty()) {
            return null; //no scale strategy
        }
        return getElasticServiceManager().getProcessingUnitScaleStrategyConfig(pu);
    }

    @Override
    public boolean isManagedByElasticServiceManager(ProcessingUnit pu) {
        if (admin.getElasticServiceManagers().isEmpty()) {
            return false;
        }
        return getElasticServiceManager().isManagingProcessingUnit(pu);
        
    }

    @Override
    public boolean isManagedByElasticServiceManagerAndScaleNotInProgress(ProcessingUnit pu) {
        if (admin.getElasticServiceManagers().isEmpty()) {
            return false;
        }
        return getElasticServiceManager().isManagingProcessingUnitAndScaleNotInProgress(pu);
    }

    @Override
    public Application deploy(ApplicationDeployment deployment) 
            throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Application deploy(ApplicationDeployment applicationDeployment, long timeout, TimeUnit timeUnit)
            throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        
        long end = System.currentTimeMillis()  + timeUnit.toMillis(timeout);
        String applicationName = applicationDeployment.getDeploymentOptions().getApplicationName();
        if (applicationName == null) {
            throw new IllegalArgumentException("Application Name cannot be null");
        }
        if (applicationName.length() == 0) {
            throw new IllegalArgumentException("Application Name cannot be an empty string");
        }
        if (admin.getApplications().getApplication(applicationName) != null) {
            throw new ApplicationAlreadyDeployedException(applicationName);
        }
        
        ProcessingUnitDeployment[] processingUnitDeployments = applicationDeployment.getDeploymentOptions().getProcessingUnitDeployments(admin);
        if (processingUnitDeployments.length == 0) {
            throw new IllegalArgumentException("Application deployment must contain at least one processing unit deployment");
        }
        
        // iterate in a deterministic order, so if deployed in parallel by another admin client, only one will succeed
        boolean timedOut = false;
        Set<String> deployedPuNames = new HashSet<String>();
        for (ProcessingUnitDeployment deployment : processingUnitDeployments) {
            try {
                long remaining = end - System.currentTimeMillis();
                if (remaining <= 0) {
                    timedOut = true;
                    break;
                }
                ProcessingUnit pu = deploy(deployment,applicationName, remaining,TimeUnit.MILLISECONDS);
                if (pu == null) {
                    timedOut = true;
                    break;
                }
                deployedPuNames.add(pu.getName());
            }
            catch (ProcessingUnitAlreadyDeployedException e) {
                if (deployedPuNames.contains(e.getProcessingUnitName())) {
                    throw new IllegalArgumentException("Application deployment contains two Processing Units with the same name " + e.getProcessingUnitName(),e);
                }
                ProcessingUnit otherPu = admin.getProcessingUnits().getProcessingUnit(e.getProcessingUnitName());
                if (otherPu != null && 
                    otherPu.getApplication() != null &&
                    otherPu.getApplication().getName().equals(applicationName)) {
                    throw new ApplicationAlreadyDeployedException(applicationName,e);
                }
                // A PU with the same name from another application (or PU not discovered yet).
                throw e;
            }
        }
        if (timedOut) {
            return null;
        }
        
        return admin.getApplications().getApplication(applicationName);
    }

    @Override
    public boolean undeployProcessingUnitsAndWait(ProcessingUnit[] processingUnits, long timeout, TimeUnit timeUnit) {
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
         
        List<GridServiceContainer> containersPendingRemoval = new ArrayList<GridServiceContainer>();
        List<ProcessingUnitInstance> instancesPendingRemoval = new ArrayList<ProcessingUnitInstance>();
        
        for (ProcessingUnit pu : processingUnits) {
            for (GridServiceContainer container : admin.getGridServiceContainers()) {
                ProcessingUnitInstance[] processingUnitInstances = container.getProcessingUnitInstances(pu.getName());
                if (processingUnitInstances.length > 0) {
                    instancesPendingRemoval.addAll(Arrays.asList(processingUnitInstances));
                    if (isManagedByElasticServiceManager(pu)) {
                        // add all containers that are managed by the elastic pu
                        containersPendingRemoval .add(container);
                    }
                }
            }    
        }
        
        final Map<String,CountDownLatch> latches = new HashMap<String,CountDownLatch>();
        for (ProcessingUnit pu : processingUnits) {
            latches.put(pu.getName(), new CountDownLatch(1));
        }
        
        ProcessingUnitRemovedEventListener listener = new ProcessingUnitRemovedEventListener() {
            public void processingUnitRemoved(ProcessingUnit removedPu) {
                CountDownLatch latch = latches.get(removedPu.getName());
                if (latch != null) {
                    latch.countDown();
                }
            }
        };
        admin.getProcessingUnits().getProcessingUnitRemoved().add(listener);
        try {
            for (final ProcessingUnit pu : processingUnits) {
                long gsmTimeout = end - System.currentTimeMillis();
                if (gsmTimeout < 0) {
                    //timeout expired
                    return false;
                }
                final InternalGridServiceManager managingGsm = (InternalGridServiceManager)pu.waitForManaged(gsmTimeout,TimeUnit.MILLISECONDS);
                if (managingGsm == null) {
                    //timeout expired
                    return false;
                }
                
                admin.scheduleAdminOperation(new Runnable() {
                    @Override
                    public void run() {
                        managingGsm.undeployProcessingUnit(pu.getName());
                    }
                });
            }
            for (ProcessingUnit pu : processingUnits) {
                try {
                    long puRemovedTimeout = end - System.currentTimeMillis();
                    if (puRemovedTimeout < 0) {
                        //timeout expired
                        return false;
                    }
                    if (!latches.get(pu.getName()).await(puRemovedTimeout, TimeUnit.MILLISECONDS)) {
                        //timeout expired
                        return false;
                    }
                } catch (InterruptedException e) {
                    throw new AdminException("Failed to undeploy", e);
                }
            }
        }finally {
            admin.getProcessingUnits().getProcessingUnitRemoved().remove(listener);
        }
        
        // use polling to determine elastic pu completed undeploy cleanup of containers (and machines)
        // and that the admin has been updated with the relevant lookup service remove events.
        while (!isUndeployComplete(processingUnits) || 
               !isAllRemoved(instancesPendingRemoval) ||
               !isAllRemoved(containersPendingRemoval) ||
               isUndeploying(instancesPendingRemoval)) {
            
            long sleepDuration = end - System.currentTimeMillis();
            if (sleepDuration < 0) {
                //timeout expired
                return false;
            }
            
            try {
                Thread.sleep(Math.min(1000, sleepDuration));
            } catch (InterruptedException e) {
                throw new AdminException("Failed to undeploy", e);
            }
        }
        return true;
    }

    private boolean isUndeploying(List<ProcessingUnitInstance> instancesPendingRemoval) {
        boolean undeploying = false;
        for (ProcessingUnitInstance instance : instancesPendingRemoval) {
            try {
                if (((InternalProcessingUnitInstance)instance).isUndeploying()) {
                    undeploying = true;
                    break;
                }
            }
            catch (AdminException e) {
                //assuming pu instance is not responding since it completed undeploy
            }
        }
        return undeploying;
    }

    private boolean isUndeployComplete(ProcessingUnit[] processingUnits) {
        boolean complete = true;
        for (ProcessingUnit pu : processingUnits) {
            // check that all pu instances have been undiscovered and not managed by ESM
            if (pu.getInstances().length > 0 || isManagedByElasticServiceManager(pu)) {
                complete = false;
                break;
            }
        }
        return complete;
    }
    
    private boolean isAllRemoved(Iterable<? extends GridComponent> componentsPendingShutdown) {
        boolean allContainersShutdown = true;    
        for (final GridComponent component : componentsPendingShutdown) {
            if (component.isDiscovered()) {
                allContainersShutdown = false;
            }
        }
        return allContainersShutdown;
    }

    @Override
    public ProcessingUnit deploy(Application application, ProcessingUnitDeploymentTopology deploymentTopology, long timeout, TimeUnit timeUnit) {
        ProcessingUnitDeployment deployment = deploymentTopology.toProcessingUnitDeployment(admin);
        return this.deploy(deployment, application.getName(), timeout, timeUnit);
    }
   
    @Override
    public String getCodeBaseURL() {
        
        String codeBaseURL = null;
        
        try {
            codeBaseURL = getCodebase(gsmAdmin);
        } catch (MalformedURLException mue) {
            throw new AdminException("Failed to retrieve codebase URL, URL is malformed.", mue);
        } catch (RemoteException re) {
            throw new AdminException("Failed to retrieve codebase URL, A remote problem occurred.", re);
        }

        return codeBaseURL;
    }
}
