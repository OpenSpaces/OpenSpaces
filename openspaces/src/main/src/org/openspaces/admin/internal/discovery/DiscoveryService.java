package org.openspaces.admin.internal.discovery;

import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.grid.gsm.GSM;
import com.j_spaces.core.service.ServiceConfigLoader;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainer;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManager;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.DefaultLookupService;
import org.openspaces.admin.internal.lus.InternalLookupService;

/**
 * @author kimchy
 */
public class DiscoveryService implements DiscoveryListener, ServiceDiscoveryListener {

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

    public void discovered(DiscoveryEvent e) {
        for (ServiceRegistrar registrar : e.getRegistrars()) {
            InternalLookupService lookupService = new DefaultLookupService(registrar, registrar.getServiceID());
            admin.addLookupService(lookupService);
        }
    }

    public void discarded(DiscoveryEvent e) {
        for (ServiceRegistrar registrar : e.getRegistrars()) {
            admin.removeLookupService(registrar.getServiceID().toString());
        }
    }

    public void serviceAdded(ServiceDiscoveryEvent event) {
        Object service = event.getPostEventServiceItem().service;
        if (service instanceof GSM) {
            InternalGridServiceManager gridServiceManager = new DefaultGridServiceManager(event.getPostEventServiceItem().serviceID, (GSM) service);
            // TODO register a listener for deployment events 
            // TODO get the currently deployed processing unit
            // TODO GSMs needs to be pinged periodically and if the ping fails for three times, simply remove it (that is because they usually start LUS as well, so we won't get service removed event)
            admin.addGridServiceManager(gridServiceManager);
        } else if (service instanceof GSC) {
            InternalGridServiceContainer gridServiceContainer = new DefaultGridServiceContainer(event.getPostEventServiceItem().serviceID, (GSC) service);
            admin.addGridServiceContainer(gridServiceContainer);
        }
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        Object service = event.getPreEventServiceItem().service;
        if (service instanceof GSM) {
            admin.removeGridServiceManager(event.getPreEventServiceItem().serviceID.toString());
        } else if (service instanceof GSC) {
            admin.removeGridServiceContainer(event.getPreEventServiceItem().serviceID.toString());
        }
    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
        // TODO do we really care about this?
    }
}
