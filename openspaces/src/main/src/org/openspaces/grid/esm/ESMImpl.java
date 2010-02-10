package org.openspaces.grid.esm;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanActivation;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.jini.rio.jsb.ServiceBeanActivation.LifeCycleManager;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;

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
import com.sun.jini.start.LifeCycle;

public class ESMImpl extends ServiceBeanAdapter implements ESM
		/*, RemoteSecuredService*//*, ServiceDiscoveryListener*/ {

	private static final String CONFIG_COMPONENT = "org.openspaces.grid.esm";
	private static final Logger logger = Logger.getLogger(CONFIG_COMPONENT);

	private final EsmExecutor esmExecutor = new EsmExecutor();
    private LifeCycle lifeCycle;
    private String[] configArgs;
	
    /**
     * Create an ESM
     */
    public ESMImpl() throws Exception {
        super();
    }

    /**
     * Create an ESM launched from the ServiceStarter framework
     */
    public ESMImpl(String[] configArgs, LifeCycle lifeCycle)
    throws Exception {
        super();
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
    }
    
    @Override
    public void advertise() throws IOException {
        super.advertise();
        logger.info("ESM started successfully with groups " + Arrays.toString(admin.getLookupGroups()) + " and locators " + Arrays.toString(admin.getLookupLocators()) + "");
    }
    
    @Override
    public synchronized void destroy(boolean force) {
        logger.info("Stopping ESM ...");
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

    public void deploy(ElasticDataGridDeployment deployment) {
        esmExecutor.deploy(deployment);
    }
}
