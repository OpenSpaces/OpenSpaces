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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.Admin;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.gsm.GridServiceManager;

import com.j_spaces.kernel.time.SystemTime;

/**
 * @author itaif
 * @since 9.0.1
 */
public class ApplicationUndeployCommand extends AbstractApplicationCommand{

        public static void main(String[] args) throws Exception {
            ApplicationUndeployCommand deployer = new ApplicationUndeployCommand();
            if (args.length < 1) {
                System.out.println(deployer.getUsage());
                return;
            }
            
            try {
                deployer.undeployAndWait(args);
            }
            catch(TimeoutException e) {
                System.exit(1);
            }
        }

        
        public void undeployAndWait(String[] args) throws TimeoutException {
            
            try {
                parseArgs(args);
                
                final GridServiceManager gsm = waitForGridServiceManager();
                
                String name = args[args.length-1];
                
                long end = SystemTime.timeMillis() + getTimeout();
                
                Application application = waitForApplication(gsm.getAdmin(), name, getTimeout(), TimeUnit.MILLISECONDS);
                
                info("Undeploying application " + name);
                final long remaining = remainingMilliseconds(name, end);
                application.undeployAndWait(remaining, TimeUnit.MILLISECONDS);
                info(name + " has been successfully undeployed.");
            }
            catch (TimeoutException e) {
                info(e.getMessage());
                throw e;
            }
        }


        private Application waitForApplication(final Admin admin, String name, long timeout, TimeUnit timeunit)
                throws TimeoutException {
            Application application;
            
            application = admin.getApplications().waitFor(name, timeout, timeunit);
            if (application == null) {    
                throw new TimeoutException("Application " + name + " discovery timed-out. Check if application is deployed.");
            }
        
            return application;
        }


        private long remainingMilliseconds(String name, long end) throws TimeoutException {
            final long remaining = end - SystemTime.timeMillis();
            if (remaining < 0) {
                throw new TimeoutException("Application " + name + " deployment timed out");
            }
            return remaining;
        }

       public String getUsage() {
            StringBuilder sb = new StringBuilder();
            if (!getManaged()) {
                sb.append("Usage: ApplicationUndeployCommand [-groups groups] [-locators hots1 hots2] [-timeout timeoutValue] [-user xxx -password yyy] [-secured true/false] Application_Name");
            } else {
                sb.append("Usage: deploy-application [-user xxx -password yyy] [-secured true/false] Application_Name");
            }
            sb.append("\n    Application_Name: The path to the application direcoty or zip file containing application.xml and the PU jars.");
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
            sb.append("\n1. Undeploy data-app");
            sb.append("\n    - Undeploys the data-app application.");
            return sb.toString();
      }
}
