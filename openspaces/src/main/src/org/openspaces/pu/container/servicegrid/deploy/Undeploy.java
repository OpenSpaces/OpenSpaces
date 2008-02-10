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
import com.j_spaces.core.Constants;
import net.jini.core.lookup.ServiceItem;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.OperationalStringManager;
import org.jini.rio.monitor.DeployAdmin;
import org.openspaces.pu.container.support.CommandLineParser;

import java.util.StringTokenizer;

/**
 * @author kimchy
 */
public class Undeploy {

    private String[] groups;

    private int lookupTimeout = 5000;

    public GSM[] findGSMs() {
        GSM[] gsms;
        ServiceItem[] result = ServiceFinder.find(null, GSM.class, lookupTimeout, getGroups());
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
                groups = new String[]{Constants.LookupManager.LOOKUP_GROUP_DEFAULT};
            }
        }
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
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
            if (param.getName().equalsIgnoreCase("timeout")) {
                setLookupTimeout(Integer.valueOf(param.getArguments()[0]));
            }
        }

        GSM[] gsms = findGSMs();
        //try undeploying using name first
        DeployAdmin primary = findDeployAdmin(gsms, puName);
        if (primary != null) {
            primary.undeploy(puName);
        } else {
            throw new Exception("Failed to find GSM to undeploy [" + puName + "]");
        }
    }

    static DeployAdmin findDeployAdmin(GSM[] items, String opstringName) {
        DeployAdmin primary = null;
        for (int i = 0; i < items.length; i++) {
            try {
                DeployAdmin deployAdmin =
                        (DeployAdmin) items[i].getAdmin();
                OperationalStringManager[] opMgrs =
                        deployAdmin.getOperationalStringManagers();
                for (int j = 0; j < opMgrs.length; j++) {
                    OperationalString opString = opMgrs[j].getOperationalString();
                    if (opString.getName().equals(opstringName) &&
                            opMgrs[j].isManaging()) {
                        primary = deployAdmin;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (primary);
    }

    public static void main(String[] args) throws Exception {
        Undeploy undeploy = new Undeploy();
        undeploy.undeploy(args);
    }
}
