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

import com.gigaspaces.grid.gsm.GSM;
import com.j_spaces.kernel.PlatformVersion;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.discovery.LookupLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.core.OperationalStringManager;
import org.jini.rio.boot.BootUtil;
import org.openspaces.pu.container.support.CommandLineParser;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author kimchy
 */
public class Undeploy {

    private static final Log logger = LogFactory.getLog(Undeploy.class);

    private String[] groups;

    private LookupLocator[] locators;

    private int lookupTimeout = 5000;

    public GSM[] findGSMs() {
        GSM[] gsms;
        logger.info("Searching for GSMs  in groups " + Arrays.asList(getGroups()) + " and locators " + Arrays.asList(getLocators()));
        ServiceItem[] result = ServiceFinder.find(null, GSM.class, lookupTimeout, getGroups(), getLocators());
        if (result != null && result.length > 0) {
            gsms = new GSM[result.length];
            for (int i = 0; i < result.length; i++) {
                gsms[i] = (GSM) result[i].service;
            }
        } else {
            gsms = new GSM[0];
        }
        return gsms;
    }

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

    public LookupLocator[] getLocators() {
        if (locators == null) {
            String locatorsProperty = java.lang.System.getProperty("com.gs.jini_lus.locators");
            if (locatorsProperty != null) {
                locators = BootUtil.toLookupLocators(locatorsProperty);
            }
        }
        return locators;
    }

    public void setLocators(String locators) {
        this.locators = BootUtil.toLookupLocators(locators);
    }

    public void setLookupTimeout(int lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    public void undeploy(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("The pu name must be defined");
        }
        String puPath = args[args.length - 1];
        int index = puPath.lastIndexOf('/');
        index = index == -1 ? 0 : index;
        String puName = puPath.substring(index);

        CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length - 1);

        // check if we have a groups parameter and timeout parameter
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("groups")) {
                setGroups(param.getArguments());
            }
            if (param.getName().equalsIgnoreCase("locators")) {
                StringBuilder sb = new StringBuilder();
                for (String arg : param.getArguments()) {
                    sb.append(arg).append(',');
                }
                setLocators(sb.toString());
            }
            if (param.getName().equalsIgnoreCase("timeout")) {
                setLookupTimeout(Integer.valueOf(param.getArguments()[0]));
            }
        }

        GSM[] gsms = findGSMs();
        //try undeploying using name first
        OperationalStringManager operationalStringManager = findDeployAdmin(gsms, puName);
        if (operationalStringManager != null) {
            operationalStringManager.undeploy();
        } else {
            throw new GSMNotFoundException(getGroups(), lookupTimeout);
        }
    }

    static OperationalStringManager findDeployAdmin(GSM[] items, String opstringName) {
        if (items.length > 0) {
            try {
                return items[0].getPrimary(opstringName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (null);
    }

    public static void main(String[] args) throws Exception {
        Undeploy undeploy = new Undeploy();
        undeploy.undeploy(args);
    }
}
