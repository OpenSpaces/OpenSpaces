package org.openspaces.admin.internal.discovery;

import com.gigaspaces.grid.gsa.AgentIdAware;
import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.grid.security.Credentials;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.operatingsystem.OSDetails;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SecurityContext;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.client.ISpaceProxy;
import com.j_spaces.core.service.ServiceConfigLoader;
import com.j_spaces.kernel.PlatformVersion;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author kimchy
 */
public class DiscoveryService implements DiscoveryListener, ServiceDiscoveryListener {

    private static final Log logger = LogFactory.getLog(DiscoveryService.class);

    private List<String> groups = null;

    private String locators = null;

    private final InternalAdmin admin;

    private volatile boolean started = false;

    private LookupDiscoveryManager ldm;
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
        Configuration config = null;
        try {
            config = ServiceConfigLoader.getConfiguration();
        } catch (ConfigurationException e) {
            throw new AdminException("Failed to get configuration for discovery service", e);
        }
        try {
            ldm = new LookupDiscoveryManager(getGroups(), getLocators(), this, config);
        } catch (Exception e) {
            throw new AdminException("Failed to start discovery service, Lookup Discovery Manager failed to start", e);
        }

        try {
            sdm = new ServiceDiscoveryManager(ldm, null, config);
        } catch (Exception e) {
            ldm.terminate();
            throw new AdminException("Failed to start discovery service, Service Discovery Manager failed to start", e);
        }

        ServiceTemplate template = new ServiceTemplate(null, null, null);
        try {
            cache = sdm.createLookupCache(template, null, this);
        } catch (Exception e) {
            sdm.terminate();
            ldm.terminate();
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
        ldm.terminate();
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
                admin.addLookupService(lookupService, nioDetails, osDetails, jvmDetails);
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
            try {
                GSM gsm = (GSM) service;
                if (gsm.isSecured()) {
                    gsm.authenticate(admin.getUsername(), admin.getPassword());
                }
                InternalGridServiceManager gridServiceManager = new DefaultGridServiceManager(serviceID, gsm, admin,
                        gsm.getAgentId(), gsm.getGSAServiceID());
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceManager.getNIODetails();
                OSDetails osDetails = gridServiceManager.getOSDetails();
                JVMDetails jvmDetails = gridServiceManager.getJVMDetails();
                admin.addGridServiceManager(gridServiceManager, nioDetails, osDetails, jvmDetails);
            } catch (Exception e) {
                logger.warn("Failed to add GSM with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof GSA) {
            try {
                GSA gsa = (GSA) service;
                if (gsa.isSecured()) {
                    gsa.authenticate(admin.getUsername(), admin.getPassword());
                }
                AgentProcessesDetails processesDetails = gsa.getDetails();
                InternalGridServiceAgent gridServiceAgent = new DefaultGridServiceAgent(serviceID, gsa, admin, processesDetails);
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceAgent.getNIODetails();
                OSDetails osDetails = gridServiceAgent.getOSDetails();
                JVMDetails jvmDetails = gridServiceAgent.getJVMDetails();
                admin.addGridServiceAgent(gridServiceAgent, nioDetails, osDetails, jvmDetails);
            } catch (Exception e) {
                logger.warn("Failed to add GSA with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof GSC) {
            try {
                GSC gsc = (GSC) service;
                if (gsc.isSecured()) {
                    gsc.authenticate(admin.getUsername(), admin.getPassword());
                }
                InternalGridServiceContainer gridServiceContainer = new DefaultGridServiceContainer(serviceID, gsc,
                        admin, gsc.getAgentId(), gsc.getGSAServiceID());
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceContainer.getNIODetails();
                OSDetails osDetails = gridServiceContainer.getOSDetails();
                JVMDetails jvmDetails = gridServiceContainer.getJVMDetails();
                admin.addGridServiceContainer(gridServiceContainer, nioDetails, osDetails, jvmDetails);
            } catch (Exception e) {
                logger.warn("Failed to add GSC with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof PUServiceBean) {
            try {
                PUServiceBean puServiceBean = (PUServiceBean) service;
                PUDetails puDetails = puServiceBean.getPUDetails();
                InternalProcessingUnitInstance processingUnitInstance = new DefaultProcessingUnitInstance(serviceID, puDetails, puServiceBean, admin);
                NIODetails nioDetails = processingUnitInstance.getNIODetails();
                OSDetails osDetails = processingUnitInstance.getOSDetails();
                JVMDetails jvmDetails = processingUnitInstance.getJVMDetails();
                admin.addProcessingUnitInstance(processingUnitInstance, nioDetails, osDetails, jvmDetails);
            } catch (Exception e) {
                logger.warn("Failed to add Processing Unit with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof IJSpace) {
            try {
                SecurityContext securityContext = null;
                if (admin.getUsername() != null) {
                    securityContext = new SecurityContext(admin.getUsername(), admin.getPassword());
                }
                IJSpace clusteredIjspace = (IJSpace) service;
                if (securityContext != null) {
                    clusteredIjspace.setSecurityContext(securityContext);
                }

                IJSpace direcyIjspace = ((ISpaceProxy) clusteredIjspace).getClusterMember();
                if (securityContext != null) {
                    direcyIjspace.setSecurityContext(securityContext);
                }

                IInternalRemoteJSpaceAdmin spaceAdmin = (IInternalRemoteJSpaceAdmin) direcyIjspace.getAdmin();

                SpaceConfig spaceConfig = spaceAdmin.getConfig();
                InternalSpaceInstance spaceInstance = new DefaultSpaceInstance(serviceID, direcyIjspace, spaceAdmin, spaceConfig, admin);
                NIODetails nioDetails = spaceInstance.getNIODetails();
                OSDetails osDetails = spaceInstance.getOSDetails();
                JVMDetails jvmDetails = spaceInstance.getJVMDetails();
                admin.addSpaceInstance(spaceInstance, clusteredIjspace, nioDetails, osDetails, jvmDetails);
            } catch (Exception e) {
                logger.warn("Failed to add Space with uid [" + serviceID + "]", e);
            }
        }
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        Object service = event.getPreEventServiceItem().service;
        ServiceID serviceID = event.getPreEventServiceItem().serviceID;
        if (service instanceof GSM) {
            admin.removeGridServiceManager(serviceID.toString());
        } else if (service instanceof GSA) {
            admin.removeGridServiceAgent(serviceID.toString());
        } else if (service instanceof GSC) {
            admin.removeGridServiceContainer(serviceID.toString());
        } else if (service instanceof PUServiceBean) {
            admin.removeProcessingUnitInstance(serviceID.toString());
        } else if (service instanceof IJSpace) {
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
