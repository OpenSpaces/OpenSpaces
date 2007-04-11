package org.openspaces.pu.container.servicegrid.deploy;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.entry.Name;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/**
 */
public class ServiceFinder {
// ------------------------------ FIELDS ------------------------------

    private static final Log logger = LogFactory.getLog(ServiceFinder.class);

// -------------------------- STATIC METHODS --------------------------

    public static ServiceItem find(String name, Class type, long wait, String[] groups) {
        ServiceItem result;
        ServiceDiscoveryManager sdm = null;

        try {
            sdm = new ServiceDiscoveryManager(
                    new LookupDiscoveryManager(groups, null, null),
                    new LeaseRenewalManager()
            );
            Entry[] attributes = null;
            if (name != null) {
                attributes = new Entry[]{
                        new Name(name)
                };
            }
            ServiceTemplate template = new ServiceTemplate(
                    null,
                    new Class[]{type},
                    attributes
            );

            logger.info("Searching for " + type.getName()
                    + (name != null ? (" named " + name) : "")
                    + " in groups "
                    + Arrays.asList(groups));
            result = sdm.lookup(template, null, wait);
        } catch (Exception e) {
            // TODO add proper exception here
            e.printStackTrace();
            result = null;
        }

        if (sdm != null) {
            sdm.terminate();
        }

        return result;
    }
}

