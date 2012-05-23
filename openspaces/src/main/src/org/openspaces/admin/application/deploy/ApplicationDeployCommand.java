/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.application.deploy;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.ApplicationFileDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;


/**
 * @author itaif
 * @since 9.0.1
 * A utility class with a main() entry point that allows deploying applications from ant or the cli
 */
public class ApplicationDeployCommand extends AbstractApplicationCommand {

        public static void main(String[] args) throws Exception {
            ApplicationDeployCommand deployer = new ApplicationDeployCommand();
            if (args.length < 1) {
                System.out.println(deployer.getUsage());
                return;
            }
            
            try {
                deployer.deployAndWait(args);
            }
            catch(TimeoutException e) {
                System.exit(1);
            }
        }

        
        public void deployAndWait(String[] args) throws TimeoutException,AdminException {
            
            try {
                parseArgs(args);
                
                final GridServiceManager gsm = waitForGridServiceManager();
                
                File applicationFolder = new File(args[args.length-1]);
                
                long end = System.currentTimeMillis() + getTimeout();
                
                ApplicationConfig applicationConfig = new ApplicationFileDeployment(applicationFolder).create();
                
                // apply security properties to each pu in the application
                for (ProcessingUnitConfigHolder puConfig : applicationConfig.getProcessingUnits()) {
                    if (getSecured() != null) {
                        puConfig.setSecured(getSecured());
                    }
                    if (getUserDetails() != null) {
                        UserDetailsConfig userDetailsConfig = new UserDetailsConfig();
                        userDetailsConfig.setUsername(getUserDetails().getUsername());
                        userDetailsConfig.setPassword(getUserDetails().getPassword());
                        puConfig.setUserDetails(userDetailsConfig);
                    }
                }
                
                String name = applicationConfig.getName();
                info("Deploying application " + name);
                
                Application dataApp = gsm.deploy(applicationConfig);
                
                for (ProcessingUnit pu : dataApp.getProcessingUnits()) {
                    long remaining = end - System.currentTimeMillis();
                    if (remaining < 0 ||
                        !pu.waitFor(pu.getTotalNumberOfInstances(), remaining, TimeUnit.MILLISECONDS)) {
                        throw new TimeoutException("Application " + name + " deployment timed out");
                    }
                }
                
                info(name + " deployment completed successfully.");
            }
            catch (TimeoutException e) {
                info(e.getMessage());
                throw e;
            }
        }

       public String getUsage() {
            StringBuilder sb = new StringBuilder();
            if (!getManaged()) {
                sb.append("Usage: ApplicationDeployCommand [-groups groups] [-locators hots1 hots2] [-timeout timeoutValue] [-user xxx -password yyy] [-secured true/false] Application_DirOrZip");
            } else {
                sb.append("Usage: deploy-application [-user xxx -password yyy] [-secured true/false] Application_DirOrZip");
            }
            sb.append("\n    Application_DirOrZip: The path to the application direcoty or zip file containing application.xml and the PU jars.");
            if (!getManaged()) {
                sb.append("\n    -groups [groupName] [groupName] ...      : The lookup groups used to look up the GSM");
                sb.append("\n    -locators [host1] [host2] ...            : The lookup locators used to look up the GSM");
                sb.append("\n    -timeout [timeout value]                 : The timeout value of GSM lookup (defaults to 5000) in milliseconds");
            }
            sb.append("\n    -user xxx -password yyyy                 : Deploys a secured processing unit propagated with the supplied user and password");
            sb.append("\n    -secured true                            : Deploys a secured processing unit (implicit when using -user/-password)");
            sb.append("\n    -deploy-timeout [timeout value in ms]    : Timeout for deploy operation, otherwise blocks until all successful/failed deployment events arrive (default)");
            sb.append("\n");
            sb.append("\n");
            sb.append("\nSome Examples:");
            if (!getManaged()) {
                sb.append("\n1. ApplicationDeployCommand examples/data/dist");
            }
            else {
                sb.append("\n1. deploy-application examples/data/dist");
            }
            sb.append("\n    - Deploys the application in the data-app directory.");
            if (!getManaged()) {
                sb.append("\n1. ApplicationDeployCommand examples/data/dist.zip");
            }
            else {
                sb.append("\n1. deploy-application examples/data/dist.zip");
            }
            sb.append("\n    - Deploys the application in the data-app zip file.");
            return sb.toString();
      }
}
