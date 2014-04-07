/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.pu.container.servicegrid.deploy;

import java.util.ArrayList;

import org.jini.rio.core.OperationalString;
import org.jini.rio.core.ServiceProvisionListener;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.logger.GSLogConfigLoader;
import com.gigaspaces.security.directory.CredentialsProvider;
import com.gigaspaces.security.directory.UserDetails;

/**
 * @author kimchy
 */
public class SpaceDeploy {

    private final Deploy deploy;

    public final static String[] validOptionsArray = Deploy.validOptionsArray;

    public SpaceDeploy() {
        this.deploy = new Deploy();
    }

    public static void setSout(boolean soutVal) {
        Deploy.setSout(soutVal);
    }

    public void initializeDiscovery(GSM gsm) {
        deploy.initializeDiscovery(gsm);
    }

    public void setGroups(String[] groups) {
        deploy.setGroups(groups);
    }

    public void setLocators(String locators) {
        deploy.setLocators(locators);
    }

    public void setSecured(boolean secured) {
        deploy.setSecured(secured);
    }

    @Deprecated
    public void setUserDetails(UserDetails userDetails) {
        deploy.setUserDetails(userDetails);
    }

    @Deprecated
    public void setUserDetails(String userName, String password) {
        deploy.setUserDetails(userName, password);
    }

    public void setCredentials(String userName, String password) {
        deploy.setCredentials(userName, password);
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        deploy.setCredentialsProvider(credentialsProvider);
    }

    public void setLookupTimeout(int lookupTimeout) {
        deploy.setLookupTimeout(lookupTimeout);
    }
    
    public void setDeployTimeout(long deployTimeout) {
        deploy.setDeployTimeout(deployTimeout);
    }

    public void deployAndWait(String[] args) throws Exception {
        deploy.deployAndWait(prepareArgs(args));
    }

    public void deploy(String[] args) throws Exception {
        deploy.deploy(prepareArgs(args));
    }

    public void deploy(String[] args, ServiceProvisionListener listener) throws Exception {
        deploy.deploy(prepareArgs(args), listener);
    }

    public OperationalString buildOperationalString(String[] args) throws Exception {
        return deploy.buildOperationalString(prepareArgs(args));
    }

    private String[] prepareArgs(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("At least space name must be defined");
        }
        String spaceName = args[args.length - 1];
        ArrayList<String> tempList = new ArrayList<String>();
        for (int i = 0; i < args.length - 1; i++) {
            tempList.add(args[i]);
        }
        tempList.add("-" + Deploy.KEY_PROPERTIES);
        tempList.add("embed://dataGridName=" + spaceName);
        tempList.add("-" + Deploy.KEY_OVERRIDE_NAME);
        tempList.add(spaceName);
        tempList.add("/templates/datagrid");
        return tempList.toArray(new String[tempList.size()]);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println(getUsage());
            return;
        }

        // init GigaSpace logger
        GSLogConfigLoader.getLoader();

        SpaceDeploy deployer = new SpaceDeploy();
        deployer.deployAndWait(args);
    }

    public static String getUsage() {
        return getUsage(false);
    }

    public static String getUsage(boolean managed) {
        StringBuilder sb = new StringBuilder();
        if (!managed) {
            sb.append("Usage: Space Deploy [-" + Deploy.KEY_SLA + " ...] [-" + Deploy.KEY_CLUSTER + " ...] [-" + Deploy.KEY_GROUPS + " groups] [-" + Deploy.KEY_LOCATORS + " host1 host2] [-" + Deploy.KEY_TIMEOUT + " timeoutValue] [-" + Deploy.KEY_PROPERTIES + " ...] [-" + Deploy.KEY_USER + " xxx -" + Deploy.KEY_PASSWORD + " yyy] [-" + Deploy.KEY_SECURED + " true/false] Space_Name");
        } else {
            sb.append("Usage: deploy-space [-" + Deploy.KEY_SLA + " ...] [-" + Deploy.KEY_CLUSTER + "...] [-" + Deploy.KEY_PROPERTIES + " ...] [-" + Deploy.KEY_USER + " xxx -" + Deploy.KEY_PASSWORD + " yyy] [-" + Deploy.KEY_SECURED + " true/false] Space_Name");
        }
        sb.append("\n    Space_Name: The name of the space to deploy");
        sb.append("\n    -" + Deploy.KEY_SLA + " [sla-location]                      : Location of an optional xml file holding the SLA element");
        sb.append("\n    -" + Deploy.KEY_CLUSTER + " [cluster properties]            : Allows to override the cluster parameters of the SLA elements");
        sb.append("\n             " + Deploy.KEY_SCHEMA + "=partitioned-sync2backup  : The cluster schema to override");
        sb.append("\n             " + Deploy.KEY_TOTAL_MEMBERS + "=1,1               : The number of instances and number of backups to override");
        if (!managed) {
            sb.append("\n    -" + Deploy.KEY_GROUPS + " [groupName] [groupName] ...      : The lookup groups used to look up the GSM");
            sb.append("\n    -" + Deploy.KEY_LOCATORS + " [host1] [host2] ...            : The lookup locators used to look up the GSM");
            sb.append("\n    -" + Deploy.KEY_TIMEOUT + " [timeout value]                 : The timeout value of GSM lookup (defaults to 5000) in milliseconds");
        }
        sb.append("\n    -" + Deploy.KEY_USER + " xxx -" + Deploy.KEY_PASSWORD + " yyyy                 : Deploys a secured space propagated with the supplied user and password");
        sb.append("\n    -" + Deploy.KEY_SECURED + " true                            : Deploys a secured space (implicit when using -user/-password)");
        sb.append("\n    -" + Deploy.KEY_PROPERTIES + " [properties-loc]             : Location of context level properties");
        sb.append("\n    -" + Deploy.KEY_PROPERTIES + " [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        sb.append("\n    -" + Deploy.KEY_MAX_INSTANCES_PER_VM + " [number]           : Allows to set the SLA number of instances per VM");
        sb.append("\n    -" + Deploy.KEY_MAX_INSTANCES_PER_MACHINE + " [number]      : Allows to set the SLA number of instances per machine");
        sb.append("\n    -" + Deploy.KEY_MAX_INSTANCES_PER_ZONE + " [zone/number,...]: Allows to set the SLA number of instances per zone");
        sb.append("\n    -" + Deploy.KEY_ZONES + " [zoneName] [zoneName] ...         : Allows to set the SLA zone requirements");
        sb.append("\n    -" + Deploy.KEY_DEPLOY_TIMEOUT + " [timeout value in ms]    : Timeout for deploy operation, otherwise blocks until all successful/failed deployment events arrive (default)");
        sb.append("\n");
        sb.append("\n");
        if (!managed) {
            sb.append("\nSome Examples:");
            sb.append("\n1. Space Deploy test");
            sb.append("\n    - Deploys a single instance space called test");
            sb.append("\n2. Deploy -" + Deploy.KEY_CLUSTER + " " + Deploy.KEY_TOTAL_MEMBERS + "=2,1 test");
            sb.append("\n    - Deploys a space called test with partitioned sync2backup cluster schema of 2 partitions, each with one backup");
            sb.append("\n3. Deploy -" + Deploy.KEY_SLA + " file://config/sla.xml test");
            sb.append("\n    - Deploys a space called test using an SLA element read from sla.xml");
        } else {
            sb.append("\nSome Examples:");
            sb.append("\n1. deploy-space test");
            sb.append("\n    - Deploys a single instance space called test");
            sb.append("\n2. deploy-space -" + Deploy.KEY_CLUSTER + " " + Deploy.KEY_TOTAL_MEMBERS + "=2,1 test");
            sb.append("\n    - Deploys a space called test with partitioned sync2backup cluster schema of 2 partitions, each with one backup");
            sb.append("\n3. deploy-space -" + Deploy.KEY_SLA + " file://config/sla.xml test");
            sb.append("\n    - Deploys a space called test using an SLA element read from sla.xml");
        }
        return sb.toString();
    }
}