package org.openspaces.grid.esm;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jini.rio.boot.BootUtil;
import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanActivation;
import org.jini.rio.jsb.ServiceBeanActivation.LifeCycleManager;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.internal.InternalAdminFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.grid.gsm.ScaleBeanServer;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;

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

public class ESMImpl<x> extends ServiceBeanAdapter implements ESM, ProcessingUnitRemovedEventListener, ProcessingUnitAddedEventListener
		/*, RemoteSecuredService*//*, ServiceDiscoveryListener*/ {

	private static final String CONFIG_COMPONENT = "org.openspaces.grid.esm";
	private static final Logger logger = Logger.getLogger(CONFIG_COMPONENT);
	private final Admin admin;
	private final MachinesSlaEnforcement machinesSlaEnforcement;
	private final ContainersSlaEnforcement containersSlaEnforcement;
	private final RebalancingSlaEnforcement rebalancingSlaEnforcement;
	private final Map<ProcessingUnit,ScaleBeanServer> scaleBeanServerPerProcessingUnit;
	private final Map<String,Map<String,String>> elasticPropertiesPerProcessingUnit;
    private LifeCycle lifeCycle;
    private String[] configArgs;
	
    /**
     * Create an ESM
     */
    public ESMImpl() throws Exception {
        super();
        
        scaleBeanServerPerProcessingUnit = new HashMap<ProcessingUnit,ScaleBeanServer>();
        elasticPropertiesPerProcessingUnit = new ConcurrentHashMap<String, Map<String,String>>();
        
        admin = new InternalAdminFactory().singleThreadedEventListeners().createAdmin();
        admin.getProcessingUnits().getProcessingUnitAdded().add(this);
        admin.getProcessingUnits().getProcessingUnitRemoved().add(this);
        
        machinesSlaEnforcement = new MachinesSlaEnforcement(admin);
        containersSlaEnforcement = new ContainersSlaEnforcement(admin);
        rebalancingSlaEnforcement = new RebalancingSlaEnforcement(admin);
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
        return scaleBeanServerPerProcessingUnit.keySet().toArray(new String[]{});
    }

    public boolean isServiceSecured() throws RemoteException {
        return false;
    }

    public SecurityContext login(UserDetails userDetails) throws SecurityException, RemoteException {
        return null;
    }

    public Map<String, String> getProcessingUnitElasticProperties(String processingUnitName) throws RemoteException {
        // uses a concurrent hashmap. Consistency not guaranteed with #setPRocessingUnitElasticConfig
        return elasticPropertiesPerProcessingUnit.get(processingUnitName);
    }

    public void setProcessingUnitElasticProperties(final String puName, final Map<String, String> properties) throws RemoteException {
        
        ((InternalAdmin)admin).scheduleNonBlockingStateChange(
                 new Runnable() {

                    public void run() {
                        ESMImpl.this.processingUnitElasticPropertiesChanged(puName,properties);
                    }
                 }
        );
    }
    
    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        try {
            ScaleBeanServer beanServer = scaleBeanServerPerProcessingUnit.remove(processingUnit.getName());
            beanServer.destroy();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to destroy ScaleBeanServer for pu " + processingUnit.getName(),e);
        }
    }

    public void processingUnitAdded(ProcessingUnit pu) {
        
        Map<String,String> puConfig = this.elasticPropertiesPerProcessingUnit.get(pu.getName());
        if (puConfig != null) {
            refreshProcessingUnitElasticConfig(pu, puConfig);
        }
        else {
            logger.info("Processing Unit " + pu.getName() + " was detected, but elastic properties for that pu was not set yet.");
        }
    }

    private void refreshProcessingUnitElasticConfig(ProcessingUnit pu, Map<String,String> elasticProperties) {

        try {
        ScaleBeanServer beanServer = scaleBeanServerPerProcessingUnit.get(pu);
        
        if (beanServer == null) {
            beanServer = new ScaleBeanServer(pu,rebalancingSlaEnforcement,containersSlaEnforcement,machinesSlaEnforcement,elasticProperties);
            scaleBeanServerPerProcessingUnit.put(pu, beanServer);
            logger.info("Elastic properties for pu " + pu.getName() + " are being enforced.");
        }
        else {
            beanServer.setElasticProperties(elasticProperties);
            logger.info("Elastic properties for pu " + pu.getName() + " are being refreshed.");
        }
        }
        catch (BeanConfigException e) {
            logger.log(Level.SEVERE,"Error configuring elasitc scale bean.",e);
        }
    }
    
    private void processingUnitElasticPropertiesChanged(String puName, Map<String,String> elasticProperties) {
        elasticPropertiesPerProcessingUnit.put(puName,elasticProperties);
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        if (pu != null) {
            refreshProcessingUnitElasticConfig(pu,elasticProperties);
        }
        else {
            logger.info("Elastic properties for pu " + puName + " has been set, but the processing unit itself was not detected yet.");
        }
    }
}
