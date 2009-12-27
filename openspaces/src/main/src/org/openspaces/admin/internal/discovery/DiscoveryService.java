package org.openspaces.admin.internal.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.DefaultElasticServiceManager;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsa.DefaultGridServiceAgent;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainer;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManager;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.DefaultLookupService;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstance;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.space.DefaultSpaceInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.pu.container.servicegrid.PUDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;

import com.gigaspaces.grid.esm.ESM;
import com.gigaspaces.grid.gsa.AgentIdAware;
import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.grid.zone.GridZoneProvider;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.jini.SharedDiscoveryManagement;
import com.j_spaces.kernel.PlatformVersion;

/**
 * @author kimchy
 */
public class DiscoveryService implements DiscoveryListener, ServiceDiscoveryListener {

    private static final Log logger = LogFactory.getLog(DiscoveryService.class);

    private List<String> groups = null;

    private String locators = null;

    private final InternalAdmin admin;

    private volatile boolean started = false;

    private ServiceDiscoveryManager sdm;
    private LookupCache cache;

    public DiscoveryService(InternalAdmin admin) {
        this.admin = admin;
    }

    public void addGroup(String group) {
        if (groups == null) {
            groups = new ArrayList<String>();
        }
        groups.add(group);
    }

    public void addLocator(String locator) {
        if (locators == null) {
            locators = locator;
        } else {
            locators += "," + locator;
        }
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        try {
            sdm = SharedDiscoveryManagement.getServiceDiscoveryManager(getGroups(), getLocators(), this);
        } catch (Exception e) {
            throw new AdminException("Failed to start discovery service, Service Discovery Manager failed to start", e);
        }

        ServiceTemplate template = new ServiceTemplate(null, null, null);
        try {
            cache = sdm.createLookupCache(template, null, this);
        } catch (Exception e) {
            sdm.terminate();
            throw new AdminException("Failed to start discovery service, Lookup Cache failed to start", e);
        }
    }

    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        cache.terminate();
        sdm.terminate();
    }

    public void discovered(DiscoveryEvent disEvent) {
        for (ServiceRegistrar registrar : disEvent.getRegistrars()) {
            try {
                InternalLookupService lookupService = new DefaultLookupService(registrar, registrar.getServiceID(), admin,
                        ((AgentIdAware) registrar.getRegistrar()).getAgentId(), ((AgentIdAware) registrar.getRegistrar()).getGSAServiceID());
                // get the details here, on the thread pool
                NIODetails nioDetails = lookupService.getNIODetails();
                OSDetails osDetails = lookupService.getOSDetails();
                JVMDetails jvmDetails = lookupService.getJVMDetails();
                admin.addLookupService(lookupService, nioDetails, osDetails, jvmDetails, ((GridZoneProvider) registrar.getRegistrar()).getZones());
            } catch (Exception e) {
                logger.warn("Failed to add lookup service with id [" + registrar.getServiceID() + "]", e);
            }
        }
    }

    public void discarded(DiscoveryEvent e) {
        for (ServiceRegistrar registrar : e.getRegistrars()) {
            admin.removeLookupService(registrar.getServiceID().toString());
        }
    }

    public void serviceAdded(ServiceDiscoveryEvent event) {
        Object service = event.getPostEventServiceItem().service;
        ServiceID serviceID = event.getPostEventServiceItem().serviceID;
        if (service instanceof GSM) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Added [GSM] with uid [" + serviceID + "]");
            }
            try {
                GSM gsm = (GSM) service;
                if (gsm.isServiceSecured()) {
                    gsm.login(admin.getUserDetails());
                }
                InternalGridServiceManager gridServiceManager = new DefaultGridServiceManager(serviceID, gsm, admin,
                        gsm.getAgentId(), gsm.getGSAServiceID());
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceManager.getNIODetails();
                OSDetails osDetails = gridServiceManager.getOSDetails();
                JVMDetails jvmDetails = gridServiceManager.getJVMDetails();
                admin.addGridServiceManager(gridServiceManager, nioDetails, osDetails, jvmDetails, gsm.getZones());
            } catch (Exception e) {
                logger.warn("Failed to add [GSM] with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof ESM) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Added [ESM] with uid [" + serviceID + "]");
            }
            try {
                ESM esm = (ESM) service;
//                if (esm.isServiceSecured()) {
//                    esm.login(admin.getUserDetails());
//                }
                InternalElasticServiceManager elasticServiceManager = new DefaultElasticServiceManager(serviceID, esm, admin,
                        esm.getAgentId(), esm.getGSAServiceID());
                // get the details here, on the thread pool
                NIODetails nioDetails = elasticServiceManager.getNIODetails();
                OSDetails osDetails = elasticServiceManager.getOSDetails();
                JVMDetails jvmDetails = elasticServiceManager.getJVMDetails();
                admin.addElasticServiceManager(elasticServiceManager, nioDetails, osDetails, jvmDetails, esm.getZones());
            } catch (Exception e) {
                logger.warn("Failed to add [ESM] with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof GSA) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Added [GSA] with uid [" + serviceID + "]");
            }
            try {
                GSA gsa = (GSA) service;
                if (gsa.isServiceSecured()) {
                    gsa.login(admin.getUserDetails());
                }
                AgentProcessesDetails processesDetails = gsa.getDetails();
                InternalGridServiceAgent gridServiceAgent = new DefaultGridServiceAgent(serviceID, gsa, admin, processesDetails);
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceAgent.getNIODetails();
                OSDetails osDetails = gridServiceAgent.getOSDetails();
                JVMDetails jvmDetails = gridServiceAgent.getJVMDetails();
                admin.addGridServiceAgent(gridServiceAgent, nioDetails, osDetails, jvmDetails, gsa.getZones());
            } catch (Exception e) {
                logger.warn("Failed to add [GSA] with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof GSC) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Added [GSC] with uid [" + serviceID + "]");
            }
            try {
                GSC gsc = (GSC) service;
                if (gsc.isServiceSecured()) {
                    gsc.login(admin.getUserDetails());
                }
                InternalGridServiceContainer gridServiceContainer = new DefaultGridServiceContainer(serviceID, gsc,
                        admin, gsc.getAgentId(), gsc.getGSAServiceID());
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceContainer.getNIODetails();
                OSDetails osDetails = gridServiceContainer.getOSDetails();
                JVMDetails jvmDetails = gridServiceContainer.getJVMDetails();
                admin.addGridServiceContainer(gridServiceContainer, nioDetails, osDetails, jvmDetails, gsc.getZones());
            } catch (Exception e) {
                logger.warn("Failed to add GSC with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof PUServiceBean) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Added [Processing Unit Instance] with uid [" + serviceID + "]");
            }
            try {
                PUServiceBean puServiceBean = (PUServiceBean) service;
                PUDetails puDetails = puServiceBean.getPUDetails();
                InternalProcessingUnitInstance processingUnitInstance = new DefaultProcessingUnitInstance(serviceID, puDetails, puServiceBean, admin);
                NIODetails nioDetails = processingUnitInstance.getNIODetails();
                OSDetails osDetails = processingUnitInstance.getOSDetails();
                JVMDetails jvmDetails = processingUnitInstance.getJVMDetails();
                admin.addProcessingUnitInstance(processingUnitInstance, nioDetails, osDetails, jvmDetails, puServiceBean.getZones());
            } catch (Exception e) {
                logger.warn("Failed to add [Processing Unit Instance] with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof IJSpace) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Added [Space Instance] with uid [" + serviceID + "]");
            }
            try {
                ISpaceProxy clusteredIjspace = (ISpaceProxy) service;
                if (clusteredIjspace.isServiceSecured()) {
                    clusteredIjspace.login(admin.getUserDetails());
                }

                ISpaceProxy direcyIjspace = (ISpaceProxy) (clusteredIjspace).getClusterMember();
                if (direcyIjspace.isServiceSecured()) {
                    direcyIjspace.login(admin.getUserDetails());
                }

                IInternalRemoteJSpaceAdmin spaceAdmin = (IInternalRemoteJSpaceAdmin) direcyIjspace.getAdmin();

                SpaceConfig spaceConfig = spaceAdmin.getConfig();
                InternalSpaceInstance spaceInstance = new DefaultSpaceInstance(serviceID, direcyIjspace, spaceAdmin, spaceConfig, admin);
                NIODetails nioDetails = spaceInstance.getNIODetails();
                OSDetails osDetails = spaceInstance.getOSDetails();
                JVMDetails jvmDetails = spaceInstance.getJVMDetails();
                admin.addSpaceInstance(spaceInstance, clusteredIjspace, nioDetails, osDetails, jvmDetails, spaceAdmin.getZones());
            } catch (Exception e) {
                logger.warn("Failed to add [Space Instance] with uid [" + serviceID + "]", e);
            }
        }
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        Object service = event.getPreEventServiceItem().service;
        ServiceID serviceID = event.getPreEventServiceItem().serviceID;
        if (service instanceof GSM) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Removed [GSM] with uid [" + serviceID + "]");
            }
            admin.removeGridServiceManager(serviceID.toString());
        } else if (service instanceof ESM) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Removed [ESM] with uid [" + serviceID + "]");
            }
            admin.removeElasticServiceManager(serviceID.toString());
        } else if (service instanceof GSA) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Removed [GSA] with uid [" + serviceID + "]");
            }
            admin.removeGridServiceAgent(serviceID.toString());
        } else if (service instanceof GSC) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Removed [GSC] with uid [" + serviceID + "]");
            }
            admin.removeGridServiceContainer(serviceID.toString());
        } else if (service instanceof PUServiceBean) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Removed [Processing Unit Instance] with uid [" + serviceID + "]");
            }
            admin.removeProcessingUnitInstance(serviceID.toString());
        } else if (service instanceof IJSpace) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service Removed [Space Instance] with uid [" + serviceID + "]");
            }
            admin.removeSpaceInstance(serviceID.toString());
        }
    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
        // TODO do we really care about this?
    }

    public String[] getGroups() {
        String[] groups;
        if (this.groups == null) {
            String groupsProperty = System.getProperty("com.gs.jini_lus.groups");
            if (groupsProperty == null) {
                groupsProperty = System.getenv("LOOKUPGROUPS");
            }
            if (groupsProperty != null) {
                StringTokenizer tokenizer = new StringTokenizer(groupsProperty);
                int count = tokenizer.countTokens();
                groups = new String[count];
                for (int i = 0; i < count; i++) {
                    groups[i] = tokenizer.nextToken();
                }
            } else {
                groups = new String[]{"gigaspaces-" + PlatformVersion.getVersionNumber()};
            }
        } else {
            groups = this.groups.toArray(new String[this.groups.size()]);
        }
        return groups;
    }

    public LookupLocator[] getLocators() {
        if (locators == null) {
            String locatorsProperty = System.getProperty("com.gs.jini_lus.locators");
            if (locatorsProperty == null) {
                locatorsProperty = System.getenv("LOOKUPLOCATORS");
            }
            if (locatorsProperty != null) {
                locators = locatorsProperty;
            }
        }
        return BootUtil.toLookupLocators(locators);
    }
}
