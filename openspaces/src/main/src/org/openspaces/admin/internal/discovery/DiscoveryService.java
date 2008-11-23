package org.openspaces.admin.internal.discovery;

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
import org.openspaces.admin.internal.admin.DefaultLookupService;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.admin.InternalLookupService;

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

        ServiceTemplate template = new ServiceTemplate(null, new Class[]{GSM.class}, null);
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
            InternalLookupService lookupService = new DefaultLookupService(registrar, registrar.getServiceID());
            admin.removeLookupService(lookupService.getUID());
        }
    }

    public void serviceAdded(ServiceDiscoveryEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
