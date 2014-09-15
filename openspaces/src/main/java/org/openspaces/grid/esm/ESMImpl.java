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
package org.openspaces.grid.esm;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.export.Exporter;

import org.jini.rio.boot.BootUtil;
import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanActivation;
import org.jini.rio.jsb.ServiceBeanActivation.LifeCycleManager;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.jini.rio.monitor.event.Event;
import org.jini.rio.monitor.event.Events;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.GridServiceAgentFailureDetectionConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.core.GigaSpace;
import org.openspaces.grid.gsm.ScaleBeanServer;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcement;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState;
import org.openspaces.grid.gsm.machines.backup.MachinesStateBackup;
import org.openspaces.grid.gsm.machines.backup.MachinesStateBackupStub;
import org.openspaces.grid.gsm.machines.backup.MachinesStateBackupToSpace;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioningAdapterFactory;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;
import org.openspaces.grid.gsm.strategy.AbstractCapacityScaleStrategyBean;
import org.openspaces.grid.gsm.strategy.ScaleStrategyBean;
import org.openspaces.grid.gsm.strategy.UndeployScaleStrategyBean;

import com.gigaspaces.grid.gsa.AgentHelper;
import com.gigaspaces.grid.zone.ZoneHelper;
import com.gigaspaces.internal.dump.InternalDumpException;
import com.gigaspaces.internal.dump.InternalDumpHelper;
import com.gigaspaces.internal.dump.InternalDumpResult;
import com.gigaspaces.internal.dump.thread.ThreadDumpProcessor;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMHelper;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.log.InternalLogHelper;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSHelper;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.internal.utils.concurrent.GSThreadFactory;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.lrmi.GenericExporter;
import com.gigaspaces.lrmi.LRMIInvocationContext;
import com.gigaspaces.lrmi.LRMIMonitoringDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoHelper;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.management.entry.JMXConnection;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.directory.CredentialsProvider;
import com.gigaspaces.security.service.RemoteSecuredService;
import com.gigaspaces.security.service.SecurityContext;
import com.gigaspaces.security.service.SecurityInterceptor;
import com.gigaspaces.security.service.SecurityResolver;
import com.gigaspaces.start.SystemBoot;
import com.j_spaces.kernel.time.SystemTime;
import com.sun.jini.start.LifeCycle;

public class ESMImpl extends ServiceBeanAdapter implements ESM, RemoteSecuredService, ProcessingUnitRemovedEventListener,
        ProcessingUnitAddedEventListener,MachineLifecycleEventListener
/*, RemoteSecuredService*//*, ServiceDiscoveryListener*/ {
	
    private static final long DISCOVERY_TIMEOUT_SECONDS = Long.getLong(EsmSystemProperties.ESM_INIT_TIMEOUT_SECONDS, EsmSystemProperties.ESM_INIT_TIMEOUT_SECONDS_DEFAULT);
    private static final long CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS= Long.getLong(EsmSystemProperties.ESM_INIT_EVENTLOOP_KEEPALIVE_ERROR_SECONDS, EsmSystemProperties.ESM_INIT_EVENTLOOP_KEEPALIVE_ERROR_SECONDS_DEFAULT);
    private static final String CONFIG_COMPONENT = "org.openspaces.grid.esm";
    private static final Logger logger = Logger.getLogger(CONFIG_COMPONENT);
    private Admin admin;
    private MachinesSlaEnforcement machinesSlaEnforcement;
    private ContainersSlaEnforcement containersSlaEnforcement;
    private RebalancingSlaEnforcement rebalancingSlaEnforcement;
    private AutoScalingSlaEnforcement autoScalingSlaEnforcement;
    private final Map<ProcessingUnit,ScaleBeanServer> scaleBeanServerPerProcessingUnit;
    private final Map<String,Map<String,String>> elasticPropertiesPerProcessingUnit;
    private final Map<String, PendingElasticPropertiesUpdate> pendingElasticPropertiesUpdatePerProcessingUnit;
    private LifeCycle lifeCycle;
    private String[] configArgs;
    private final NonBlockingElasticMachineProvisioningAdapterFactory nonBlockingAdapterFactory;
    private final EventsStore eventsStore;
    private final AtomicBoolean adminInitialized = new AtomicBoolean(false);
    private final AtomicBoolean destroyStarted = new AtomicBoolean(false);
    private SecurityInterceptor securityInterceptor;
    private AtomicLong keepAlive = new AtomicLong(0);
    private final long adminInitializationTimeout;
    private final Map<String, Set<Remote>> exportedApis = new HashMap<String, Set<Remote>>();
    protected GigaSpace managementSpace;
    private MachinesStateBackup machinesStateBackup;
    
    /**
     * Create an ESM
     */
    public ESMImpl() throws Exception {
        super();
        nonBlockingAdapterFactory = new NonBlockingElasticMachineProvisioningAdapterFactory();        
        scaleBeanServerPerProcessingUnit = new HashMap<ProcessingUnit,ScaleBeanServer>();
        elasticPropertiesPerProcessingUnit = new ConcurrentHashMap<String, Map<String,String>>();
        pendingElasticPropertiesUpdatePerProcessingUnit = new ConcurrentHashMap<String, PendingElasticPropertiesUpdate>();
        eventsStore = new EventsStore();
        //Discovery warm-up period
        new ESMImplInitializer(new ESMImplInitializer.AdminCreatedEventListener() {

            @Override
            public void adminCreated(Admin admin, GigaSpace managementSpace) {

                ESMImpl.this.admin = admin;
                startKeepAlive(admin);
                startKeepAliveMonitor();
                
                ESMImpl.this.managementSpace = managementSpace;                
                final MachinesSlaEnforcementState machinesSlaEnforcementState = new MachinesSlaEnforcementState();
                ESMImpl.this.machinesSlaEnforcement = new MachinesSlaEnforcement(machinesSlaEnforcementState);
                ESMImpl.this.containersSlaEnforcement = new ContainersSlaEnforcement(admin);
                ESMImpl.this.rebalancingSlaEnforcement = new RebalancingSlaEnforcement();
                ESMImpl.this.autoScalingSlaEnforcement = new AutoScalingSlaEnforcement(admin);
                if (managementSpace == null) {
                    ESMImpl.this.machinesStateBackup = new MachinesStateBackupStub();
                }
                else {
                    ESMImpl.this.machinesStateBackup = new MachinesStateBackupToSpace(admin, managementSpace, machinesSlaEnforcementState);
                }

                adminInitialized.set(true);
                
                while (!isEsmDiscovered(admin)) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                //triggers initialization of all PU SLA beans
                admin.getProcessingUnits().getProcessingUnitAdded().add(ESMImpl.this);
                admin.getProcessingUnits().getProcessingUnitRemoved().add(ESMImpl.this);
                
                logger.info("ESM is now listening for Processing Unit events");
            }


            
            private boolean isEsmDiscovered(Admin admin) {
                ElasticServiceManager[] esms = admin.getElasticServiceManagers().getManagers();
                if (esms.length == 0) {
                    logger.log(Level.INFO, "Waiting to discover one ESM");
                    return false;
                }

                if (esms.length > 1) {
                    logger.log(Level.INFO, "Waiting for one of the ESMs to stop. Currently running " + esms.length + " ESMs");
                    return false;
                }

                for (ElasticServiceManager esm : admin.getElasticServiceManagers()) {
                    if (esm.isDiscovered() && esm.getAgentId() != -1 && esm.getGridServiceAgent() == null) {
                        logger.log(Level.INFO, "Waiting to discover GSA that started ESM " + esm.getUid());
                        return false;
                    }
                }
                return true;
            }
            
        });
        
        adminInitializationTimeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(DISCOVERY_TIMEOUT_SECONDS);
        logger.fine("Starting ESM initialization, timeout in " + DISCOVERY_TIMEOUT_SECONDS + " seconds.");
        
    }

        
    /**
     * Create an ESM launched from the ServiceStarter framework
     */
    public ESMImpl(String[] configArgs, LifeCycle lifeCycle)
    throws Exception {
        this();
        this.lifeCycle = lifeCycle;
        this.configArgs = configArgs;
        bootstrap(configArgs);
    }

    protected void bootstrap(String[] configArgs) throws Exception {
        try {

            /* Configure a FaultDetectionHandler for the ESM */
            String fdh = "org.openspaces.grid.esm.ESMFaultDetectionHandler";
            Object[] fdhConfigArgs = new Object[]{new String[]{
                    "-",
                    "org.openspaces.grid.esm.ESMFaultDetectionHandler.invocationDelay=" + System.getProperty("org.openspaces.grid.esm.ESMFaultDetectionHandler.invocationDelay", "1000"),
                    "org.openspaces.grid.esm.ESMFaultDetectionHandler.retryCount=" + System.getProperty("org.openspaces.grid.esm.ESMFaultDetectionHandler.retryCount", "1"),
                    "org.openspaces.grid.esm.ESMFaultDetectionHandler.retryTimeout=" + System.getProperty("org.openspaces.grid.esm.ESMFaultDetectionHandler.retryTimeout", "500")
            }
            };
            ClassBundle faultDetectionHandler =
                new org.jini.rio.core.ClassBundle(fdh,
                        null,  // load from classpath
                        new String[]{"setConfiguration"},
                        new Object[]{ fdhConfigArgs });

            context = ServiceBeanActivation.getServiceBeanContext(
                    CONFIG_COMPONENT,
                    "ESM",
                    "Service Grid Infrastructure",
                    "com.gigaspaces.grid:type=ESM",
                    faultDetectionHandler,
                    configArgs,
                    getClass().getClassLoader());
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Getting ServiceElement", e);
            throw e;
        }
        try {
            start(context);
            LifeCycleManager lMgr = (LifeCycleManager)context
            .getServiceBeanManager().getDiscardManager();
            lMgr.register(getServiceProxy(), context);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Register to LifeCycleManager", e);
            throw e;
        }
    }

	private boolean isAdminInitializationTimedOut() {
		return System.currentTimeMillis() > adminInitializationTimeout;
	}
	
    @Override
    public synchronized void initialize(ServiceBeanContext context) throws Exception {
        if (SecurityResolver.isSecurityEnabled())
            securityInterceptor = new SecurityInterceptor("grid");

        while (!adminInitialized.get()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Waiting for ESM initializer to complete");
            }
            SystemBoot.exitIfHasAgentAndAgentIsNotRunning();
            if (isAdminInitializationTimedOut()) {
                //see GS-11463, could be fatal issue with LRMI listener port that requires process restart.
                throw new ESMInitializationTimeoutException(DISCOVERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            Thread.sleep(1000);
        }
        logger.info("Starting ESM ...");
        super.initialize(context);

        /* Get the JMX Service URL */
        String jmxServiceURL = SystemBoot.getJMXServiceURL();
        if (jmxServiceURL != null) {
            String hostName = BootUtil.getHostAddress();
            int port = SystemBoot.getRegistryPort();
            String name = context.getServiceElement().getName() +
            "_" +
            hostName +
            "_" +
            port;
            addAttribute(new JMXConnection(jmxServiceURL, name));
        }
    }

    @Override
    public void advertise() throws IOException {
        super.advertise();
        logger.info("ESM started successfully with groups " + Arrays.toString(super.admin.getLookupGroups()) + " and locators " + Arrays.toString(super.admin.getLookupLocators()) + "");
    }

    @Override
    public synchronized void destroy(boolean force) {
        logger.info("Stopping ESM ...");
        destroyStarted.set(true);
        machinesStateBackup.close();
        admin.getProcessingUnits().getProcessingUnitRemoved().remove(this);
        admin.getProcessingUnits().getProcessingUnitAdded().remove(this);
        synchronized (scaleBeanServerPerProcessingUnit) {
            for (ScaleBeanServer beanServer : scaleBeanServerPerProcessingUnit.values()) {
                beanServer.destroy();
            }
            this.scaleBeanServerPerProcessingUnit.clear();
        }
        admin.close();
        if (lifeCycle != null) {
            lifeCycle.unregister(this);
        }
        super.destroy(force);
        logger.info("ESM stopped successfully");
    }

    @Override
    protected Object createProxy() {
        Object proxy;
        if (securityInterceptor != null) {
            proxy = SecuredESMProxy.getInstance((ESM)getExportedProxy(), getUuid());
        } else {
            proxy = ESMProxy.getInstance((ESM)getExportedProxy(), getUuid());
        }
        return(proxy);
    }

    public int getAgentId() throws RemoteException {
        return AgentHelper.getAgentId();
    }

    public String getGSAServiceID() throws RemoteException {
        return AgentHelper.getGSAServiceID();
    }

    public NIODetails getNIODetails() throws RemoteException {
        return NIOInfoHelper.getDetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return NIOInfoHelper.getNIOStatistics();
    }
    
    @Override
    public void enableLRMIMonitoring() throws RemoteException {
        NIOInfoHelper.enableMonitoring();
    }
    
    @Override
    public void disableLRMIMonitoring() throws RemoteException {
        NIOInfoHelper.disableMonitoring();
    }
    
    @Override
    public LRMIMonitoringDetails fetchLRMIMonitoringDetails() throws RemoteException {
        return NIOInfoHelper.fetchMonitoringDetails();
    }

    public long getCurrentTimestamp() throws RemoteException {
        return System.currentTimeMillis();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return OSHelper.getDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return OSHelper.getStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return JVMHelper.getDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return JVMHelper.getStatistics();
    }

    public void runGc() throws RemoteException {
        System.gc();
    }

    public String[] getZones() throws RemoteException {
        return ZoneHelper.getSystemZones();
    }

    public LogEntries logEntriesDirect(LogEntryMatcher matcher)
    throws RemoteException, IOException {
        return InternalLogHelper.logEntriesDirect(LogProcessType.ESM, matcher);
    }

    public byte[] dumpBytes(String file, long from, int length)
    throws RemoteException, IOException {
        return InternalDumpHelper.dumpBytes(file, from, length);
    }

    public InternalDumpResult generateDump(String cause,
            Map<String, Object> context) throws RemoteException,
            InternalDumpException {
        if (context == null) {
            context = new HashMap<String, Object>();
        }
        context.put("esm", this);
        return InternalDumpHelper.generateDump(cause, context);
    }

    public InternalDumpResult generateDump(String cause,
            Map<String, Object> context, String... contributors)
    throws RemoteException, InternalDumpException {
        if (context == null) {
            context = new HashMap<String, Object>();
        }
        context.put("esm", this);
        return InternalDumpHelper.generateDump(cause, context, contributors);
    }

    public String[] getManagedProcessingUnits() {
        Set<ProcessingUnit> puSet = scaleBeanServerPerProcessingUnit.keySet();
        String [] puNames = new String[puSet.size()];
        int i = 0;
        for (ProcessingUnit pu : puSet) {
            puNames[i] = pu.getName();
            i++;
        }
        return puNames;
    }

    public boolean isServiceSecured() throws RemoteException {
        return securityInterceptor != null;
    }

    public SecurityContext login(CredentialsProvider credentialsProvider) throws SecurityException, RemoteException {
        throw new SecurityException("Invalid method call."); //this is proxy API
    }

    @Override
    public SecurityContext login(SecurityContext securityContext) throws RemoteException {
        if (isServiceSecured())
            return securityInterceptor.authenticate(securityContext);
        else
            return null;
    }

    public void setProcessingUnitScaleStrategy(final String puName, final ScaleStrategyConfig scaleStrategyConfig) {
        logger.fine("setting scale strategy for " + puName);
        submitAndWait(new Callable<Void>() {

                @Override
                public Void call() {
                    Map<String, String> properties = elasticPropertiesPerProcessingUnit.get(puName);
                    
                    if (properties == null)
                    {
                        properties = scaleStrategyConfig.getProperties();
                        //If there are no properties yet, this is a race condition. We received scale command before the ProcessingUnitAdded
                        //event, keep the scale command for later merge
                        pendingElasticPropertiesUpdatePerProcessingUnit.put(puName, 
                                new PendingElasticPropertiesUpdate(scaleStrategyConfig.getBeanClassName(), properties));
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("Stored elastic properties for " + puName + " (elasticProperties.size()=" + properties.size()+")");
                        }
                    }
                    else
                    {
                        mergeScaleProperties(scaleStrategyConfig.getBeanClassName(), scaleStrategyConfig.getProperties(), properties);
                        ESMImpl.this.processingUnitElasticPropertiesChanged(puName,properties);
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("Merged elastic properties for " + puName + " (elasticProperties.size()=" + properties.size()+")");
                        }
                    }
                    return null;
                }
            }
        );
    }
    
    public Events getScaleStrategyEvents(final long cursor, final int maxNumberOfEvents) {
        logger.fine("get scale strategy events cursor=" + cursor + " maxNumberOfEvents=" + maxNumberOfEvents);
        try {
            return submitAndWait(new Callable<Events>() {
    
                @Override
                public Events call() throws Exception {
                    return eventsStore.getEventsFromCursor(cursor, maxNumberOfEvents);
                }
            });
        }
        catch (IllegalStateException e) {
            if (destroyStarted.get() || e instanceof EsmNotInitializedException) {
                //going down, gracefully return no events
                return new Events(cursor,new Event[] {});
            }
            throw e;
        }
     }
    /**
     * Note: Not thread safe. Call only from submitAndWait
     */
    private ScaleStrategyBean getScaleStrategyBean(String puName) {
        if (!pendingElasticPropertiesUpdatePerProcessingUnit.containsKey(puName)) {
            for(Entry<ProcessingUnit,ScaleBeanServer> pair : scaleBeanServerPerProcessingUnit.entrySet())
                if (pair.getKey().getName().equals(puName)) {
                final ScaleBeanServer beanServer = pair.getValue();        
                if (beanServer.getEnabledBean() != null) {
                    return beanServer.getEnabledBean();
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    <T> T submitAndWait(final Callable<T> task) {
        if (!adminInitialized.get()) {
            throw new EsmNotInitializedException();
        }
        final AtomicReference<Object> future = new AtomicReference<Object>();
        final CountDownLatch latch = new CountDownLatch(1);
        ((InternalAdmin)admin).scheduleNonBlockingStateChange(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            future.set(task.call());
                        } catch (Throwable e) {
                            future.set(e);
                        }
                        finally {
                            latch.countDown();
                        }
                    }
                    
                }
                );
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        Object result = future.get();
        if (result instanceof RuntimeException) {
            throw (RuntimeException)result;
        }
            
        if (result instanceof Error) {
            throw (Error)result;
        }
        if (result instanceof Exception) {
            throw new IllegalStateException("Unexpected exception",((Exception)result));
        }
        return (T)result;
    }

    private void mergeScaleProperties(final String strategyClassName, final Map<String, String> strategyProperties,
            Map<String, String> properties) {
        ScaleStrategyBeanPropertiesManager propertiesManager = new ScaleStrategyBeanPropertiesManager(properties);
        propertiesManager.disableAllBeans();
        propertiesManager.setBeanConfig(strategyClassName, strategyProperties);
        propertiesManager.enableBean(strategyClassName);
    }

    @Override
    public void setProcessingUnitElasticProperties(final String puName, final Map<String, String> properties) throws RemoteException {
        logger.fine("Queuing elastic properties for " + puName);
        submitAndWait(new Callable<Void>() {
                @Override
                public Void call() {
                    Map<String, String> properties = elasticPropertiesPerProcessingUnit.get(puName);
                    if (properties == null)
                    {
                        //If there are no properties yet, this is a race condition. We received set elastic properties command before the ProcessingUnitAdded
                        //event, keep the set command for later override
                        pendingElasticPropertiesUpdatePerProcessingUnit.put(puName, new PendingElasticPropertiesUpdate(new HashMap<String, String>(0)));
                    }
                    else
                    {
                        ESMImpl.this.processingUnitElasticPropertiesChanged(puName,properties);
                    }
                    return null;                  
              }
            }
        );
    }

    @Override
    public void processingUnitRemoved(final ProcessingUnit pu) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Processing Unit Removed " + pu.getName());
        }
        
        pendingElasticPropertiesUpdatePerProcessingUnit.remove(pu.getName());
        final ScaleBeanServer beanServer = scaleBeanServerPerProcessingUnit.get(pu);        
        if (beanServer != null) {
            logger.info("Processing Unit " + pu.getName() + " was removed. Cleaning up machines."); 
            beanServer.undeploy();
            elasticPropertiesPerProcessingUnit.remove(pu.getName());
        }
    }

    @Override
    public void processingUnitAdded(ProcessingUnit pu) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Processing Unit Added " + pu.getName());
        }
        
        ScaleBeanServer undeployedBeanServer = scaleBeanServerPerProcessingUnit.remove(pu);
        if (undeployedBeanServer != null) {
        	ScaleStrategyBean scaleStrategyBean = getScaleStrategyBean(pu.getName());
            if (scaleStrategyBean != null && scaleStrategyBean instanceof AbstractCapacityScaleStrategyBean) {
    			// unexport external apis that might have existed in services
    			// with the same name and were uninstalled.
    			unexportExistingApis(pu.getName());
    			
            			}
            //TODO: Check isSlaMet() and handle this case ?!@
            undeployedBeanServer.destroy();
        }

        InternalProcessingUnit internalPu = (InternalProcessingUnit) pu;
        Map<String, String> elasticProperties = internalPu.getElasticProperties();
        if (!elasticProperties.isEmpty())
        {
            //If we have a pending update elastic properties command due to race condition
            if (pendingElasticPropertiesUpdatePerProcessingUnit.containsKey(pu.getName()))
            {
                PendingElasticPropertiesUpdate pendingPropsUpdate = pendingElasticPropertiesUpdatePerProcessingUnit.remove(pu.getName());
                
                if (pendingPropsUpdate.isScaleCommand()) {
                    // Pending operation is scale command, merge changes
                    mergeScaleProperties(pendingPropsUpdate.getStrategyClassName(), pendingPropsUpdate.getElasticProperties(), elasticProperties);
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Added " + pu.getName() + " and merged elastic properties (elasticProperties.size()=" + elasticProperties.size()+")");
                    }
                }
                else {
                    //Pending operation is override command (setElasticProperties), override with pending state.
                    elasticProperties = pendingPropsUpdate.getElasticProperties();
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Added " + pu.getName() + " and overridden elastic properties (elasticProperties.size()=" + elasticProperties.size()+")");
                    }
                }
            }
            elasticPropertiesPerProcessingUnit.put(pu.getName(), elasticProperties);        
            refreshProcessingUnitElasticConfig(pu, elasticProperties);
        }
    }


	private void unexportExistingApis(final String processingUnitName) {
		final Exporter exporter = getExporter();
		final Set<Remote> apis = this.exportedApis.get(processingUnitName);
		if (apis != null) {
			for (Remote api : apis) {
				try {
					if (exporter instanceof GenericExporter) {
						((GenericExporter)exporter).unexport(api);
					} else {
						throw new IllegalStateException("exporter must be an instance of GenericExporter");
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Failed unexporting api " + api.getClass().getName() + " instance with hashCode=" 
								+ api.hashCode() + " : " + e.getMessage(), e);
				}
			}
		}
	}

    private void refreshProcessingUnitElasticConfig(ProcessingUnit pu, Map<String,String> elasticProperties) {

        try {

            if (pu.getRequiredZones().length != 1) {
                throw new BeanConfigurationException("Processing Unit must have exactly one container zone defined.");
            }

            if (elasticProperties.size() == 0) {
                throw new BeanConfigurationException("elasticProperties for " + pu.getName() + " cannot be empty. Could all GSMs have been restarted?");
            }
            
            ScaleBeanServer beanServer = scaleBeanServerPerProcessingUnit.get(pu);

            if (beanServer == null) {
                ProcessingUnitSchemaConfig schemaConfig = new ProcessingUnitSchemaConfig(elasticProperties);
                ElasticMachineIsolationConfig isolationConfig = new ElasticMachineIsolationConfig(elasticProperties);
                beanServer = new ScaleBeanServer(pu,schemaConfig, rebalancingSlaEnforcement,containersSlaEnforcement,machinesSlaEnforcement, autoScalingSlaEnforcement, nonBlockingAdapterFactory, isolationConfig, eventsStore, machinesStateBackup);
                scaleBeanServerPerProcessingUnit.put(pu, beanServer);
            }
            //TODO: Move this to a separate thread since bean#afterPropertiesSet() might block. This is very tricky since the PU can deploy/undeploy while we are in that thread, which changes the bean server.
            beanServer.setElasticProperties(elasticProperties);
            //don't log properties since it may contain passwords
            logger.info("Elastic properties for pu " + pu.getName() + " are being enforced.");
        }
        catch (BeanConfigException e) {
            machinesSlaEnforcement.failedRecoveredStateOnEsmStart(pu);
            logger.log(Level.SEVERE,"Error configuring elastic scale bean for pu " + pu.getName(),e);
        }
    }

    private void processingUnitElasticPropertiesChanged(String puName, Map<String,String> elasticProperties) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Processing Unit Elastic Properties Changed for " + puName);
        }
        
        elasticPropertiesPerProcessingUnit.put(puName,elasticProperties);
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        if (pu != null) {
            //todo: move gsm update inside bean
            InternalGridServiceManager managinGsm = (InternalGridServiceManager)pu.getManagingGridServiceManager();
            managinGsm.updateProcessingUnitElasticPropertiesOnGsm(pu, elasticProperties);
            refreshProcessingUnitElasticConfig(pu,elasticProperties);
        }
        else {
            logger.info("Elastic properties for pu " + puName + " has been set, but the processing unit itself was not detected yet.");
        }
    }

    @Override
    public void machineAdded(Machine machine) {
        machine.getOperatingSystem().startStatisticsMonitor();
    }

    @Override
    public void machineRemoved(Machine machine) {
        machine.getOperatingSystem().stopStatisticsMonitor();
    }

    public static class PendingElasticPropertiesUpdate 
    {

        private final String _strategyClassName;
        private final Map<String, String> _elasticProperties;
        private final boolean _scaleCommand;

        public PendingElasticPropertiesUpdate(String strategyClassName, Map<String, String> strategyProperties) {
            _strategyClassName = strategyClassName;
            _elasticProperties = strategyProperties;
            _scaleCommand = true;
        }

        public PendingElasticPropertiesUpdate(Map<String, String> properties) {
            _strategyClassName = null;
            _elasticProperties = properties;
            _scaleCommand = false;
        }

        public String getStrategyClassName() {
            return _strategyClassName;
        }

        public Map<String, String> getElasticProperties() {
            return _elasticProperties;
        }

        public boolean isScaleCommand() {
            return _scaleCommand;
        }

    }

    @Override
    public ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(final String processingUnitName) throws RemoteException {

        return submitAndWait( new Callable<ScaleStrategyConfig>() {

            @Override
            public ScaleStrategyConfig call() {

                ScaleStrategyConfig config = null;
                ScaleStrategyBean scaleStrategyBean = getScaleStrategyBean(processingUnitName);
                if (scaleStrategyBean != null) {
                    config = scaleStrategyBean.getConfig();
                }
                return config;
            }
            
        });
    }
    
    @Override
    public boolean isManagingProcessingUnit(final String processingUnitName) throws RemoteException {
        return submitAndWait(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                boolean isManaging = false;
                ScaleStrategyBean scaleStrategyBean = getScaleStrategyBean(processingUnitName);
                if (scaleStrategyBean != null) {
                    if (scaleStrategyBean instanceof UndeployScaleStrategyBean) {
                        if (scaleStrategyBean.isScaleInProgress()) {
                            // undeploy is still in progress
                            isManaging = true;
                        }
                    }
                    else {
                        isManaging = true;
                    }
                }
                return isManaging;
            }
        });
    }

    @Override
    public boolean isManagingProcessingUnitAndScaleNotInProgress(final String processingUnitName) throws RemoteException {
        return submitAndWait(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                ScaleStrategyBean scaleStrategyBean = getScaleStrategyBean(processingUnitName);
                return scaleStrategyBean != null && !scaleStrategyBean.isScaleInProgress();
            }
        });
    }

    @Override
    public Remote getRemoteApi(final String processingUnitName, final String apiName) throws RemoteException {
    	//TODO: yom bet cloudDriver.init() + security;
    	return submitAndWait(new Callable<Remote>() {
            @Override
            public Remote call() throws Exception {
                ScaleStrategyBean scaleStrategyBean = getScaleStrategyBean(processingUnitName);
                Remote api = null;
                if (scaleStrategyBean != null && scaleStrategyBean instanceof AbstractCapacityScaleStrategyBean) {
                	try {
                		api = ((AbstractCapacityScaleStrategyBean)scaleStrategyBean).getRemoteApi(apiName);
                		if (api != null) {
                			if (logger.isLoggable(Level.FINE)) {
                				logger.fine("Exporting "+ apiName +" api of processing unit " + processingUnitName 
                						+ ". hasCode=" + api.hashCode());
                			}
                			// save a reference to the exported apis.
                			Set<Remote> existingSet = exportedApis.get(processingUnitName);
                			if (existingSet != null) {
                				existingSet.add(api);
                			} else {
                				existingSet = new LinkedHashSet<Remote>();
                				existingSet.add(api);
                				exportedApis.put(processingUnitName, existingSet);
                		}
                			
                			return getExporter().export(api);  
                		}
                	} catch (Exception e) {
                		if (api != null) {
                			logger.log(Level.WARNING, "Failed exporting cloud "+ apiName +" api instance with hashCode=" 
                					+ api.hashCode() + " : " + e.getMessage() , e);
                		}
                	}
                }
                return null;
            }
        });
   }
    
    /**
     * @see MachinesSlaEnforcementEndpoint#disableAgentFailureDetection(GridServiceAgent)
     */
    @Override
    public void disableAgentFailureDetection(final String processingUnitName, final long timeout, final TimeUnit timeunit) throws RemoteException {
    	final long expireTimestamp = System.currentTimeMillis() + timeunit.toMillis(timeout);
    	changeAgentFailureDetection(processingUnitName, expireTimestamp, false);
    }
    
    /**
     * @see MachinesSlaEnforcementEndpoint#enableAgentFailureDetection(GridServiceAgent)
     */
    @Override
    public void enableAgentFailureDetection(final String processingUnitName) throws RemoteException {
    	changeAgentFailureDetection(processingUnitName, null, true);
    }
    
    private void changeAgentFailureDetection(final String processingUnitName, final Long expireTimestamp, final boolean enable) {
    	final InetSocketAddress clientEndPointAddress = LRMIInvocationContext.getCurrentContext().getClientEndPointAddress();
		final String ipAddress = clientEndPointAddress.getAddress().getHostAddress();
    	submitAndWait(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
            	
            	// validate input
                final ScaleStrategyBean scaleStrategyBean = getScaleStrategyBean(processingUnitName);
                if (scaleStrategyBean == null) {
                	throw new IllegalArgumentException("Not managing " + processingUnitName);
                }
                
                if (!(scaleStrategyBean instanceof AbstractCapacityScaleStrategyBean)) {
                	throw new IllegalArgumentException(processingUnitName + " does not support disabling of agent failover");
                }
                
                Map<String, String> elasticProperties = elasticPropertiesPerProcessingUnit.get(processingUnitName);
                if (elasticProperties == null) {
                	throw new IllegalArgumentException("Could not find configuration for " + processingUnitName);
                }

                // Change pu properties, copy before modifying it.
                final HashMap<String, String> newProperties = new HashMap<String,String>(elasticProperties);
				GridServiceAgentFailureDetectionConfig agentFailureDetectionConfig = new GridServiceAgentFailureDetectionConfig(newProperties);
                if (enable) {
                	agentFailureDetectionConfig.enableFailureDetection(ipAddress);
                	logger.info("Enabling agent failure detection for " + processingUnitName + " on machine " + ipAddress);
                }
                else {
                	agentFailureDetectionConfig.disableFailureDetection(ipAddress, expireTimestamp);
                	logger.info("Disabling agent failure detection for " + processingUnitName + " on machine " + ipAddress);
                }
                processingUnitElasticPropertiesChanged(processingUnitName, newProperties);
				return null;
            }
    	});
    }

	private void startKeepAlive(Admin admin) {
		((InternalAdmin) admin).scheduleWithFixedDelayNonBlockingStateChange(
				new Runnable() {
					public void run() {
						try {
							checkKeepAlive();
						} catch (InternalKeepAliveEventDelayed e) {
							// esm.xml will cause the gsa to crash this process
							// when this is printed.
							logger.log(Level.SEVERE, "Event loop error", e);
						} finally {
							keepAlive.set(System.currentTimeMillis());
							if (logger.isLoggable(Level.FINEST)) {
								logger.log(
										Level.FINEST,
										"Event Loop Keepalive "
												+ keepAlive.get());
							}
						}
					}
				}, 0, CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS / 2,
				TimeUnit.SECONDS);
	}

	private void startKeepAliveMonitor() {
		createKeepAliveMonitorExecutor().scheduleWithFixedDelay(
				new Runnable() {

					@Override
					public void run() {
						try {
							checkKeepAlive();
						} catch (InternalKeepAliveEventDelayed e) {
							logThreads();
							// esm.xml will cause the gsa to crash this process
							// when this is printed.
							logger.log(
									Level.SEVERE,
									"Event loop error. Investigate GS-admin-event-executor-thread in the logged thread dump.",
									e);
						}
					}
				}, 0, CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS / 10,
				TimeUnit.SECONDS);
	}

	private void checkKeepAlive() {
		final long now = SystemTime.timeMillis();
		final long lastKeepalive = keepAlive.get();
		if (lastKeepalive != 0) {
			final long delaySeconds = (now - lastKeepalive) / 1000;
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, "Event Loop Keepalive delaySeconds="
						+ delaySeconds + " threshold="
						+ CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS);
			}
			if (delaySeconds > CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS) {
				logger.log(Level.INFO, "Event Loop was delayed " + delaySeconds
						+ "seconds.");
				throw new InternalKeepAliveEventDelayed(delaySeconds);
			}
		}
	}

	private void logThreads() {
		final ThreadDumpProcessor tdp = new ThreadDumpProcessor();
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		try {
			tdp.processDeadlocks(pw);
			tdp.processAllThreads(pw);
		} catch (final Exception e) {
			logger.log(Level.SEVERE, "Failed to dump threads", e);
		} finally {
			try {
				pw.close();
			} catch (final Exception e) {
				logger.log(Level.FINE, "Failed to dump threads", e);
			}
			logger.info(sw.toString());
		}
	}

	private ScheduledThreadPoolExecutor createKeepAliveMonitorExecutor() {
		return createScheduledThreadPoolExecutor("esm-keep-alive-monitor", 1,
				true);
	}

	private ScheduledThreadPoolExecutor createScheduledThreadPoolExecutor(
			String threadName, int numberOfThreads, boolean useDaemonThreads) {
		final ClassLoader correctClassLoader = Thread.currentThread()
				.getContextClassLoader();
		ScheduledThreadPoolExecutor executorService = (ScheduledThreadPoolExecutor) Executors
				.newScheduledThreadPool(numberOfThreads, new GSThreadFactory(
						threadName, useDaemonThreads) {
					@Override
					public Thread newThread(Runnable r) {
						Thread thread = super.newThread(r);
						thread.setContextClassLoader(correctClassLoader);
						return thread;
					}
				});
		return executorService;
	}
}
