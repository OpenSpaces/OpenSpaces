package org.openspaces.admin.internal.discovery;

import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.operatingsystem.OSDetails;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.service.ServiceConfigLoader;
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
import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
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

/**
 * @author kimchy
 */
public class DiscoveryService implements DiscoveryListener, ServiceDiscoveryListener {

    private static final Log logger = LogFactory.getLog(DiscoveryService.class);

    private String[] groups;

    private LookupLocator[] locators;

    private InternalAdmin admin;

    private volatile boolean started = false;

    private LookupDiscoveryManager ldm;
    private ServiceDiscoveryManager sdm;
    private LookupCache cache;

    public DiscoveryService(String[] groups, LookupLocator[] locators, InternalAdmin admin) {
        this.groups = groups;
        this.locators = locators;
        this.admin = admin;
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
            ldm = new LookupDiscoveryManager(groups, locators, this, config);
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
            InternalLookupService lookupService = new DefaultLookupService(registrar, registrar.getServiceID(), admin);
            try {
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
                InternalGridServiceManager gridServiceManager = new DefaultGridServiceManager(serviceID, (GSM) service, admin);
                // get the details here, on the thread pool
                NIODetails nioDetails = gridServiceManager.getNIODetails();
                OSDetails osDetails = gridServiceManager.getOSDetails();
                JVMDetails jvmDetails = gridServiceManager.getJVMDetails();
                admin.addGridServiceManager(gridServiceManager, nioDetails, osDetails, jvmDetails);
            } catch (Exception e) {
                logger.warn("Failed to add GSM with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof GSC) {
            try {
                InternalGridServiceContainer gridServiceContainer = new DefaultGridServiceContainer(serviceID, (GSC) service, admin);
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
                InternalProcessingUnitInstance processingUnitInstance = new DefaultProcessingUnitInstance(serviceID, puDetails, puServiceBean);
                admin.addProcessingUnitInstance(processingUnitInstance);
            } catch (Exception e) {
                logger.warn("Failed to add Processing Unit with uid [" + serviceID + "]", e);
            }
        } else if (service instanceof IJSpace) {
            try {
                IJSpace ijspace = (IJSpace) service;
                IInternalRemoteJSpaceAdmin spaceAdmin = (IInternalRemoteJSpaceAdmin) ijspace.getAdmin();
                SpaceConfig spaceConfig = spaceAdmin.getConfig();
                InternalSpaceInstance spaceInstance = new DefaultSpaceInstance(serviceID, ijspace, spaceAdmin, spaceConfig);
                admin.addSpaceInstance(spaceInstance);
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
}
