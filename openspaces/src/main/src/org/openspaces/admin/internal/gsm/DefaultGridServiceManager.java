/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.gsm;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.core.OperationalString;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.monitor.ProvisionMonitorAdmin;
import org.jini.rio.monitor.event.Events;
import org.jini.rio.resources.servicecore.ServiceAdmin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.ApplicationAlreadyDeployedException;
import org.openspaces.admin.application.ApplicationDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
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
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.topology.ElasticStatefulProcessingUnitConfigHolder;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;
import org.openspaces.admin.space.ElasticSpaceDeployment;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.core.util.FileUtils;
import org.openspaces.pu.container.servicegrid.deploy.Deploy;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.internal.utils.StringUtils;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.lrmi.nio.LRMIServiceMonitoringDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.directory.User;

/**
 * @author kimchy
 */
public class DefaultGridServiceManager extends AbstractAgentGridComponent implements InternalGridServiceManager {

    private static final Log logger = LogFactory.getLog(DefaultGridServiceManager.class);
    
    private final ServiceID serviceID;

    private final GSM gsm;

    private final ProvisionMonitorAdmin gsmAdmin;

    private long eventsCursor = 0;

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
        return deploy(deployment.create(), timeout, timeUnit);
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment) {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(MemcachedDeployment deployment) {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit) {
        return deploy(deployment.create(), timeout, timeUnit);
    }

    public ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) {
        String applicationName = null;
        return deploy(deployment, applicationName, timeout, timeUnit);
    }
    
    private ProcessingUnit deploy(ProcessingUnitDeployment deployment, String applicationName, long timeout,
            TimeUnit timeUnit) {
        return deploy(deployment.create(), applicationName, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deploy(ProcessingUnitConfigHolder puConfigHolder) {
        return deploy(puConfigHolder, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }
    
    @Override
    public ProcessingUnit deploy(ProcessingUnitConfigHolder puConfigHolder, long timeout, TimeUnit timeUnit) {
        String applicationName = null;
        return deploy(puConfigHolder, applicationName, timeout, timeUnit);
    }
    
    private ProcessingUnit deploy(ProcessingUnitConfigHolder puConfigHolder, String applicationName, long timeout, TimeUnit timeUnit) {
        return deploy(toProcessingUnitConfig(puConfigHolder), applicationName, timeout, timeUnit);
    }
    
    private ProcessingUnit deploy(ProcessingUnitConfig puConfig, String applicationName, long timeout, TimeUnit timeUnit) {
        
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
        if (puConfig.getSecured() != null) {
            deploy.setSecured(puConfig.getSecured());
        }
        UserDetailsConfig userDetailsConfig = puConfig.getUserDetails();
        if (userDetailsConfig != null) {
            deploy.setUserDetails(new User(userDetailsConfig.getUsername(), userDetailsConfig.getPassword()));
        }
        deploy.setApplicationName(applicationName);
        final OperationalString operationalString;
        try {
            operationalString = deploy.buildOperationalString(puConfig.toDeploymentOptions());
        } catch (Exception e) {
            throw new AdminException("Failed to deploy [" + puConfig.getProcessingUnit() + "]", e);
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
            throw new AdminException("Failed to deploy [" + puConfig.getProcessingUnit() + "]", e);
        } finally {
            Deploy.setDisableInfoLogging(false);
            getAdmin().getProcessingUnits().getProcessingUnitAdded().remove(added);
        }
        
        if (!puConfig.getElasticProperties().isEmpty()) {
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
            if (logger.isDebugEnabled()) {
                logger.debug("Decrementing planned instance if pending of " + processingUnit.getName());
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("Decrementing instance " + ((InternalProcessingUnitInstance) processingUnitInstance).getServiceID() + " of " + processingUnitInstance.getProcessingUnit().getName());
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("Incrementing instance of " + processingUnit.getName());
            }
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
            throw new AdminException("No privileges to relocate a processing unit instance "+ processingUnitInstance.getProcessingUnitInstanceName() , se);
        } catch (Exception e) {
            String gsc = "GSC-"+gridServiceContainer.getAgentId()+"["+gridServiceContainer.getVirtualMachine().getDetails().getPid()+"]@" + gridServiceContainer.getMachine().getHostName();
            throw new AdminException("Failed to relocate processing unit instance "+ processingUnitInstance.getProcessingUnitInstanceName() + " to " + gsc, e);
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
    
    @Override
    public void enableLRMIMonitoring() throws RemoteException {
        gsm.enableLRMIMonitoring();
    }
    
    @Override
    public void disableLRMIMonitoring() throws RemoteException {
        gsm.disableLRMIMonitoring();
    }
    
    @Override
    public LRMIServiceMonitoringDetails[] fetchLRMIServicesMonitoringDetails() throws RemoteException {
        return gsm.fetchLRMIServicesMonitoringDetails();
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
        return deploy(deployment.create(), admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(ElasticSpaceDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment.create(),timeout,timeUnit);
    }

    public ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment,admin.getDefaultTimeout(),admin.getDefaultTimeoutTimeUnit());
    }

    public ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        
        return deploy(deployment.create(),timeout,timeUnit);
    }

    public ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment)
        throws ProcessingUnitAlreadyDeployedException {
    
        return deploy(deployment,admin.getDefaultTimeout(),admin.getDefaultTimeoutTimeUnit());
    }
    
    public ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
        throws ProcessingUnitAlreadyDeployedException {

        return deploy(deployment.create(),timeout,timeUnit);
    }
    
    @Override
    public void setProcessingUnitElasticProperties(ProcessingUnit pu, Map<String,String> properties) {
        getElasticServiceManager().setProcessingUnitElasticProperties(pu, properties);
    }
    
    public void setProcessingUnitScaleStrategyConfig(ProcessingUnit pu, ScaleStrategyConfig scaleStrategyConfig) {
        getElasticServiceManager().setProcessingUnitScaleStrategyConfig(pu, scaleStrategyConfig);
    }
        
    @Override
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
    public Application deploy(ApplicationConfig applicationConfig) 
            throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        return deploy(applicationConfig, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Application deploy(ApplicationDeployment applicationDeployment, long timeout, TimeUnit timeUnit)
            throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        return deploy(applicationDeployment.create(), timeout, timeUnit);
    }
    
    
    @Override
    public Application deploy(ApplicationConfig applicationConfig, long timeout, TimeUnit timeUnit)
            throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        long end = System.currentTimeMillis()  + timeUnit.toMillis(timeout);
        String applicationName = applicationConfig.getName();
        if (applicationName == null) {
            throw new IllegalArgumentException("Application Name cannot be null");
        }
        if (applicationName.length() == 0) {
            throw new IllegalArgumentException("Application Name cannot be an empty string");
        }
        if (admin.getApplications().getApplication(applicationName) != null) {
            throw new ApplicationAlreadyDeployedException(applicationName);
        }
        
        ProcessingUnitConfigHolder[] processingUnitConfigHolders = applicationConfig.getProcessingUnits();
        if (processingUnitConfigHolders.length == 0) {
            throw new AdminException("Application must contain at least one processing unit.");
        }
        
        //(if necessary) unzip applicaiton.zip to temp directory 
        File tempDirectory = null;
        File jarsDirectory = applicationConfig.getJarsDirectoryOrZip();
        if (jarsDirectory != null && jarsDirectory.isFile()) {
            tempDirectory = FileUtils.unzipToTempFolder(applicationConfig.getJarsDirectoryOrZip());
            jarsDirectory = tempDirectory;
        }
        
        try {
        // iterate in a deterministic order, so if deployed in parallel by another admin client, only one will succeed
        boolean timedOut = false;
        Set<String> deployedPuNames = new HashSet<String>();
        for (ProcessingUnitConfigHolder puConfigHolder : processingUnitConfigHolders) {
            try {
                long remaining = end - System.currentTimeMillis();
                if (remaining <= 0) {
                    timedOut = true;
                    break;
                }
                
                final ProcessingUnitConfig puConfig = toProcessingUnitConfig(puConfigHolder);
            
                //handle relative paths to jar files
                boolean isAbsolutePath = new File(puConfig.getProcessingUnit()).isAbsolute();
                boolean isRelativeToGSHomedir = puConfig.getProcessingUnit().trim().startsWith("/");
                boolean isAddDirectory = !isAbsolutePath && !isRelativeToGSHomedir;
                        
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "puConfig.getProcessingUnit()="+puConfig.getProcessingUnit()+" "+
                            "isAbsolutePath="+isAbsolutePath+" "+
                            "isRelativeToGSHomedir="+isRelativeToGSHomedir+" "+
                            "isAddDirectory=" +isAddDirectory);
                }
                if (jarsDirectory != null && isAddDirectory) {
                    File jar = new File(
                            jarsDirectory,
                            puConfig.getProcessingUnit());
                    puConfig.setProcessingUnit(jar.getAbsolutePath());
                }
                                
                //deploy pu
                ProcessingUnit pu = deploy(puConfig, applicationName, remaining,TimeUnit.MILLISECONDS);
                if (pu == null) {
                    timedOut = true;
                    break;
                }
                deployedPuNames.add(pu.getName());
            }
            catch (ProcessingUnitAlreadyDeployedException e) {
                if (deployedPuNames.contains(e.getProcessingUnitName())) {
                    throw new AdminException("Application deployment contains two Processing Units with the same name " + e.getProcessingUnitName(),e);
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
        finally {
            if (tempDirectory != null) {
                FileUtils.deleteFileOrDirectory(tempDirectory);
            }
        }
    }

    private ProcessingUnitConfig toProcessingUnitConfig(ProcessingUnitConfigHolder puConfigHolder) {
        if (puConfigHolder instanceof ElasticStatefulProcessingUnitConfigHolder) {
            ((ElasticStatefulProcessingUnitConfigHolder) puConfigHolder).setAdmin(admin);
        }
        final ProcessingUnitConfig puConfig = puConfigHolder.toProcessingUnitConfig();
        return puConfig;
    }

    public boolean undeployProcessingUnitsAndWait(ProcessingUnit[] processingUnits, long timeout, TimeUnit timeUnit) {
        try {
            undeployProcessingUnitsAndWaitInternal(processingUnits, timeout, timeUnit);
            return true;
        }
        catch (TimeoutException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed undeploying processing units " + processingUnitsToString(processingUnits),e);
            }
            return false;
        }
        catch (InterruptedException e) {
            throw new AdminException("Failed undeploying processing units " + processingUnitsToString(processingUnits), e);
        }
    }
    
    private String processingUnitsToString(ProcessingUnit[] processingUnits) {
        String[] puNames = new String[processingUnits.length];
        for (int i = 0 ; i < processingUnits.length ; i++) {
            puNames[i] = processingUnits[i].getName();
        }
        return StringUtils.arrayToCommaDelimitedString(puNames);
    }

    private void undeployProcessingUnitsAndWaitInternal(ProcessingUnit[] processingUnits, long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
         
        List<GridServiceContainer> containersPendingRemoval = new ArrayList<GridServiceContainer>();
        List<ProcessingUnitInstance> puInstancesPendingRemoval = new ArrayList<ProcessingUnitInstance>();
        List<SpaceInstance> spaceInstancesPendingRemoval = new ArrayList<SpaceInstance>();
        
        for (ProcessingUnit pu : processingUnits) {
            for (GridServiceContainer container : admin.getGridServiceContainers()) {
                ProcessingUnitInstance[] processingUnitInstances = container.getProcessingUnitInstances(pu.getName());
                if (processingUnitInstances.length > 0) {
                    puInstancesPendingRemoval.addAll(Arrays.asList(processingUnitInstances));
                    for (ProcessingUnitInstance puInstance : processingUnitInstances) {
                        SpaceInstance spaceInstance = puInstance.getSpaceInstance();
                        if (spaceInstance != null) {
                            spaceInstancesPendingRemoval.add(spaceInstance);
                        }
                    }
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
                    throw new TimeoutException("Timeout expired before udeploying processing unit " + pu);
                }
                final InternalGridServiceManager managingGsm = (InternalGridServiceManager)pu.waitForManaged(gsmTimeout,TimeUnit.MILLISECONDS);
                if (managingGsm == null) {
                    throw new TimeoutException("Timeout expired while waiting for GSM that manages processing unit " + pu);
                }
                
                admin.scheduleAdminOperation(new Runnable() {
                    @Override
                    public void run() {
                        managingGsm.undeployProcessingUnit(pu.getName());
                    }
                });
            }
            for (ProcessingUnit pu : processingUnits) {
                long puRemovedTimeout = end - System.currentTimeMillis();
                if (puRemovedTimeout < 0) {
                    throw new TimeoutException("Timeout expired before waiting for processing unit " + pu + " to undeploy");
                }
                if (!latches.get(pu.getName()).await(puRemovedTimeout, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException("Timeout expired while waiting for processing unit " + pu + " to undeploy");
                }
            }
        }finally {
            admin.getProcessingUnits().getProcessingUnitRemoved().remove(listener);
        }
        
        // use polling to determine elastic pu completed undeploy cleanup of containers (and machines)
        // and that the admin has been updated with the relevant lookup service remove events.
        while (true) {
            try {
                verifyUndeployComplete(processingUnits); 
                verifyNotDiscovered(puInstancesPendingRemoval);
                verifyNotDiscovered(spaceInstancesPendingRemoval);
                verifyNotDiscovered(containersPendingRemoval);
                verifyInstancesNotUndeploying(puInstancesPendingRemoval);
                break;
            }
            catch (TimeoutException e) {
                long sleepDuration = end - System.currentTimeMillis();
                if (sleepDuration < 0) {
                    throw e;
                }
                //suppress and retry
                Thread.sleep(Math.min(1000, sleepDuration));
            }             
        }
    }

    private void verifyInstancesNotUndeploying(List<ProcessingUnitInstance> instancesPendingRemoval) throws TimeoutException {
        
        for (ProcessingUnitInstance instance : instancesPendingRemoval) {
            if (isProcessingUnitInstanceUndeploying(instance)) {
                throw new TimeoutException("Instance "+ instance.getProcessingUnitInstanceName() + " UID="+instance.getUid() + " is still undeploying.");
            }
        }
    }

    private boolean isProcessingUnitInstanceUndeploying(ProcessingUnitInstance instance) {
        boolean isUndeploying = false;
        try {
            isUndeploying = ((InternalProcessingUnitInstance)instance).isUndeploying();
        }
        catch (final AdminException e) {
            //assuming pu instance is not responding since it completed undeploy
        }
        return isUndeploying;
    }

    private void verifyUndeployComplete(ProcessingUnit[] processingUnits) throws TimeoutException {
        
        for (ProcessingUnit pu : processingUnits) {
            // check that all pu instances have been undiscovered and not managed by ESM
            int numberOfInstances = pu.getInstances().length;
            if (numberOfInstances > 0) {
                throw new TimeoutException(pu.getName() + " is still undeploying " + numberOfInstances + " instances");
            }
            
            if (pu.getSpace() != null) {
                throw new TimeoutException(pu.getName() + " still has an embedded space");
            }
            
            if (isManagedByElasticServiceManager(pu)) {
                throw new TimeoutException(pu.getName() + " undeployment is in progress (removing containers and machines if applicable)");
            }
        }
    }
    
    private void verifyNotDiscovered(Iterable<? extends GridComponent> componentsPendingShutdown) throws TimeoutException {
        for (final GridComponent component : componentsPendingShutdown) {
            if (component.isDiscovered()) {
                throw new TimeoutException(component.getUid() + " is still discovered");
            }
        }
    }

    @Override
    public ProcessingUnit deploy(Application application, ProcessingUnitDeploymentTopology deploymentTopology, long timeout, TimeUnit timeUnit) {
        ProcessingUnitConfigHolder configFactory = deploymentTopology.create();
        return this.deploy(configFactory, application.getName(), timeout, timeUnit);
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
    
    @Override
    public Events getEvents(int maxEvents) {
        Events events = gsm.getEvents(eventsCursor, maxEvents);
        eventsCursor = events.getNextCursor();
        return events;
    }
}
