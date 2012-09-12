/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.servicegrid.deploy;

import com.j_spaces.core.service.ServiceConfigLoader;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.BackwardsServiceDiscoveryManager;
import net.jini.lookup.entry.Name;

/**
 */
public class ServiceFinder {
// ------------------------------ FIELDS ------------------------------

// -------------------------- STATIC METHODS --------------------------

    public static ServiceItem[] find(String name, Class type, long wait, String[] groups, LookupLocator[] locators) {
        ServiceItem[] result;
        BackwardsServiceDiscoveryManager sdm = null;

        try {
            sdm = new BackwardsServiceDiscoveryManager(
                    new LookupDiscoveryManager(groups, locators, null, ServiceConfigLoader.getConfiguration()),
                    new LeaseRenewalManager(),
                    ServiceConfigLoader.getConfiguration()
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

            result = sdm.lookup(template, 1, 1,  null, wait);
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

