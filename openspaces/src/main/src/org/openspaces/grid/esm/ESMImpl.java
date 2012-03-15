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
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jini.rio.boot.BootUtil;
import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanActivation;
import org.jini.rio.jsb.ServiceBeanActivation.LifeCycleManager;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.jini.rio.monitor.event.Events;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.InternalAdminFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.grid.gsm.ScaleBeanServer;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioningAdapterFactory;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;
import org.openspaces.grid.gsm.strategy.ScaleStrategyBean;
import org.openspaces.grid.gsm.strategy.UndeployScaleStrategyBean;

import com.gigaspaces.grid.gsa.AgentHelper;
import com.gigaspaces.grid.zone.ZoneHelper;
import com.gigaspaces.internal.dump.InternalDumpException;
import com.gigaspaces.internal.dump.InternalDumpHelper;
import com.gigaspaces.internal.dump.InternalDumpResult;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMHelper;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.log.InternalLogHelper;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSHelper;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoHelper;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.management.entry.JMXConnection;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.directory.UserDetails;
import com.gigaspaces.security.service.SecurityContext;
import com.gigaspaces.start.SystemBoot;
import com.sun.jini.start.LifeCycle;

public class ESMImpl extends ServiceBeanAdapter implements ESM, ProcessingUnitRemovedEventListener, ProcessingUnitAddedEventListener,MachineLifecycleEventListener
/*, RemoteSecuredService*//*, ServiceDiscoveryListener*/ {


    private static final long CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS=60;
    private static final String CONFIG_COMPONENT = "org.openspaces.grid.esm";
    private static final Logger logger = Logger.getLogger(CONFIG_COMPONENT);
    private final Admin admin;
    private final MachinesSlaEnforcement machinesSlaEnforcement;
    private final ContainersSlaEnforcement containersSlaEnforcement;
    private final RebalancingSlaEnforcement rebalancingSlaEnforcement;
    private final Map<ProcessingUnit,ScaleBeanServer> scaleBeanServerPerProcessingUnit;
    private final Map<String,Map<String,String>> elasticPropertiesPerProcessingUnit;
    private final Map<String, PendingElasticPropertiesUpdate> pendingElasticPropertiesUpdatePerProcessingUnit;
    private LifeCycle lifeCycle;
    private String[] configArgs;
    private final NonBlockingElasticMachineProvisioningAdapterFactory nonBlockingAdapterFactory;
    private final EventsStore eventsStore;

    /**
     * Create an ESM
     */
    public ESMImpl() throws Exception {
        super();
        nonBlockingAdapterFactory = new NonBlockingElasticMachineProvisioningAdapterFactory();        
        scaleBeanServerPerProcessingUnit = new HashMap<ProcessingUnit,ScaleBeanServer>();
        elasticPropertiesPerProcessingUnit = new ConcurrentHashMap<String, Map<String,String>>();
        pendingElasticPropertiesUpdatePerProcessingUnit = new ConcurrentHashMap<String, PendingElasticPropertiesUpdate>();

        admin = new InternalAdminFactory().singleThreadedEventListeners().createAdmin();
        
        final long delay = CHECK_SINGLE_THREAD_EVENT_PUMP_EVERY_SECONDS*1000/2;
        final long delayError = delay; 
        ((InternalAdmin)admin).scheduleWithFixedDelayNonBlockingStateChange(
                new Runnable() {
                    long timestamp = 0;
                    public void run() {
                        long now = System.currentTimeMillis();
                        if (timestamp == 0 || 
                                now-timestamp - delay < delayError) {
                        }
                        else {
                            logger.warning(
                                    "Single threaded admin keep alive event has been delayed by " + 
                                    (now-timestamp-delay)/1000 + " seconds.");
                        }
                        timestamp = now;
                    }
                }
                , 0, delay, TimeUnit.MILLISECONDS);
        machinesSlaEnforcement = new MachinesSlaEnforcement(admin);
        containersSlaEnforcement = new ContainersSlaEnforcement(admin);
        rebalancingSlaEnforcement = new RebalancingSlaEnforcement();
        eventsStore = new EventsStore();
        //Discovery warm-up period
        new ESMImplInitializer(admin, new Runnable() {

            @Override
            public void run() {
                //triggers initialization of all PU SLA beans
                ESMImpl eventListener = ESMImpl.this;
                admin.getProcessingUnits().getProcessingUnitAdded().add(eventListener);
                admin.getProcessingUnits().getProcessingUnitRemoved().add(eventListener);
            }
            
        });
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

    @Override
    public synchronized void initialize(ServiceBeanContext context) throws Exception {
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
        logger.info("ESM started successfully with groups " + Arrays.toString(admin.getGroups()) + " and locators " + Arrays.toString(admin.getLocators()) + "");
    }

    @Override
    public synchronized void destroy(boolean force) {
        logger.info("Stopping ESM ...");

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
        Object proxy = ESMProxy.getInstance((ESM)getExportedProxy(), getUuid());
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
        return false;
    }

    public SecurityContext login(UserDetails userDetails) throws SecurityException, RemoteException {
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
                        //If there are no properties yet, this is a race condition. We received scale command before the ProcessingUnitAdded
                        //event, keep the scale command for later merge
                        pendingElasticPropertiesUpdatePerProcessingUnit.put(puName, 
                                new PendingElasticPropertiesUpdate(scaleStrategyConfig.getBeanClassName(), scaleStrategyConfig.getProperties()));
                    }
                    else
                    {
                        mergeScaleProperties(scaleStrategyConfig.getBeanClassName(), scaleStrategyConfig.getProperties(), properties);
                        ESMImpl.this.processingUnitElasticPropertiesChanged(puName,properties);
                    }
                    return null;
                }
            }
        );
    }
    
    public Events getScaleStrategyEvents(final long cursor, final int maxNumberOfEvents) {
        logger.fine("get scale strategy events cursor=" + cursor + " maxNumberOfEvents=" + maxNumberOfEvents);
        return submitAndWait(new Callable<Events>() {

            @Override
            public Events call() throws Exception {
                return eventsStore.getEventsFromCursor(cursor, maxNumberOfEvents);
            }
        });
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

    public void processingUnitRemoved(final ProcessingUnit pu) {

        pendingElasticPropertiesUpdatePerProcessingUnit.remove(pu.getName());
        final ScaleBeanServer beanServer = scaleBeanServerPerProcessingUnit.get(pu);        
        if (beanServer != null) {
            logger.info("Processing Unit " + pu.getName() + " was removed. Cleaning up machines."); 
            beanServer.undeploy();
            elasticPropertiesPerProcessingUnit.remove(pu.getName());
        }
    }

    public void processingUnitAdded(ProcessingUnit pu) {

        ScaleBeanServer undeployedBeanServer = scaleBeanServerPerProcessingUnit.remove(pu);
        if (undeployedBeanServer != null) {
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
                //Pending operation is scale command, merge changes
                if (pendingPropsUpdate.isScaleCommand())
                    mergeScaleProperties(pendingPropsUpdate.getStrategyClassName(), pendingPropsUpdate.getElasticProperties(), elasticProperties);
                //Pending operation is override command (setElasticProperties), override with pending state.
                else
                    elasticProperties = pendingPropsUpdate.getElasticProperties();
            }
            elasticPropertiesPerProcessingUnit.put(pu.getName(), elasticProperties);        
            refreshProcessingUnitElasticConfig(pu, elasticProperties);
        }
    }

    private void refreshProcessingUnitElasticConfig(ProcessingUnit pu, Map<String,String> elasticProperties) {

        try {

            if (pu.getRequiredZones().length != 1) {
                throw new BeanConfigurationException("Processing Unit must have exactly one container zone defined.");
            }

            ScaleBeanServer beanServer = scaleBeanServerPerProcessingUnit.get(pu);

            if (beanServer == null) {
                ProcessingUnitSchemaConfig schemaConfig = new ProcessingUnitSchemaConfig(elasticProperties);
                ElasticMachineIsolationConfig isolationConfig = new ElasticMachineIsolationConfig(elasticProperties);
                beanServer = new ScaleBeanServer(pu,schemaConfig, rebalancingSlaEnforcement,containersSlaEnforcement,machinesSlaEnforcement,nonBlockingAdapterFactory, isolationConfig, eventsStore);
                scaleBeanServerPerProcessingUnit.put(pu, beanServer);
            }
            //TODO: Move this to a separate thread since bean#afterPropertiesSet() might block. This is very tricky since the PU can deploy/undeploy while we are in that thread, which changes the bean server.
            beanServer.setElasticProperties(elasticProperties);
            //don't log properties since it may contain passwords
            logger.info("Elastic properties for pu " + pu.getName() + " are being enforced.");
        }
        catch (BeanConfigException e) {
            logger.log(Level.SEVERE,"Error configuring elasitc scale bean.",e);
        }
    }

    private void processingUnitElasticPropertiesChanged(String puName, Map<String,String> elasticProperties) {
        elasticPropertiesPerProcessingUnit.put(puName,elasticProperties);
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        if (pu != null) {
            InternalGridServiceManager managinGsm = (InternalGridServiceManager)pu.getManagingGridServiceManager();
            managinGsm.updateProcessingUnitElasticPropertiesOnGsm(pu, elasticProperties);
            refreshProcessingUnitElasticConfig(pu,elasticProperties);
        }
        else {
            logger.info("Elastic properties for pu " + puName + " has been set, but the processing unit itself was not detected yet.");
        }
    }

    public void machineAdded(Machine machine) {
        machine.getOperatingSystem().startStatisticsMonitor();
    }

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

}
