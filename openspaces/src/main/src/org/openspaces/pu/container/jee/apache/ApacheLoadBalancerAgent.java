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

package org.openspaces.pu.container.jee.apache;

import com.j_spaces.kernel.PlatformVersion;
import com.j_spaces.kernel.SecurityPolicyLoader;
import com.j_spaces.core.Constants;
import com.gigaspaces.logger.GSLogConfigLoader;

import java.util.StringTokenizer;
import java.util.Arrays;

import org.openspaces.pu.container.support.CommandLineParser;
import org.jini.rio.boot.BootUtil;
import org.apache.commons.logging.Log;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryEvent;

/**
 * @author kimchy
 */
public class ApacheLoadBalancerAgent implements DiscoveryListener, ServiceDiscoveryListener {

    private String[] groups;

    private String locators;


    private LookupDiscoveryManager ldm;
    private ServiceDiscoveryManager sdm;
    private LookupCache cache;

    public String[] getGroups() {
        if (groups == null) {
            String groupsProperty = java.lang.System.getProperty("com.gs.jini_lus.groups");
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
        }
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public String getLocators() {
        if (locators == null) {
            String locatorsProperty = java.lang.System.getProperty("com.gs.jini_lus.locators");
            if (locatorsProperty != null) {
                locators = locatorsProperty;
            }
        }
        return locators;
    }

    public void setLocators(String locators) {
        this.locators = locators;
    }

    public void start() throws Exception {
        ldm = new LookupDiscoveryManager(getGroups(), BootUtil.toLookupLocators(getLocators()), this);

        sdm = new ServiceDiscoveryManager(ldm, null);
        cache = sdm.createLookupCache(null, null, this);

        System.out.println("Starting Apache Load Balancer Agent");
        System.out.println("Groups " + Arrays.toString(getGroups()) + ", Locators [" + locators + "]");
    }

    public void stop() {
        System.out.println("Stopping Apached Load Balancer Agent");
    }

    public void discovered(DiscoveryEvent event) {
        System.out.println("LUS Discovered [" + event + "]");
    }

    public void discarded(DiscoveryEvent event) {
        System.out.println("LUS Discarded [" + event + "]");
    }

    public void serviceAdded(ServiceDiscoveryEvent event) {
        System.out.println("Service Added [" + event + "]");
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        System.out.println("Service Removed [" + event + "]");
    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
        // don't care about this one
    }

    public static void main(String[] args) throws Exception {
        GSLogConfigLoader.getLoader();
        if (System.getProperty("java.security.policy") == null) {
            SecurityPolicyLoader.loadPolicy(Constants.System.SYSTEM_GS_POLICY);
        }
        final ApacheLoadBalancerAgent agent = new ApacheLoadBalancerAgent();

        CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length - 1);
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("groups")) {
                agent.setGroups(param.getArguments());
            }
            if (param.getName().equalsIgnoreCase("locators")) {
                StringBuilder sb = new StringBuilder();
                for (String arg : param.getArguments()) {
                    sb.append(arg).append(',');
                }
                agent.setLocators(sb.toString());
            }
        }

        try {
            agent.start();

            // Use the MAIN thread as the non daemon thread to keep it alive
            final Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        agent.stop();
                    } finally {
                        mainThread.interrupt();
                    }
                }
            });
            while (!mainThread.isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    // do nothing, simply exit
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
