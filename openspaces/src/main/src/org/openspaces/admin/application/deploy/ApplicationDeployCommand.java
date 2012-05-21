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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.ApplicationFileDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;
import org.openspaces.pu.container.support.CommandLineParser;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;


/**
 * @author itaif
 * @since 9.0.1
 * A utility class with a main() entry point that allows deploying applications from ant or the cli
 */
public class ApplicationDeployCommand {

        private static final Log logger = LogFactory.getLog(ApplicationDeployCommand.class);

        private String[] groups;

        private String locators;

        private int lookupTimeout = 5000;
        
        private long deployTimeout = Long.MAX_VALUE;

        private boolean sout = false;

        private boolean disableInfoLogging = false;

        public void setDisableInfoLogging(boolean disableInfoLogging) {
            this.disableInfoLogging = disableInfoLogging;
        }

        public void setSout(boolean sout) {
            this.sout = sout;
        }

        public void setGroups(String[] groups) {
            this.groups = groups;
        }

        public void setLocators(String locators) {
            this.locators = locators;
        }

        public void setLookupTimeout(int lookupTimeout) {
            this.lookupTimeout = lookupTimeout;
        }

        private Boolean secured;

        public void setSecured(boolean secured) {
            this.secured = secured;
        }
        
        private UserDetails userDetails;

        private boolean managed = false;

        public void setUserDetails(UserDetails userDetails) {
            this.userDetails = userDetails;
        }

        public void setUserDetails(String userName, String password) {
            this.userDetails = new User(userName, password);
        }
        
        public void setDeployTimeout(long deployTimeout) {
            this.deployTimeout = deployTimeout;
        }
        
        public void setManaged(boolean managed) {
            this.managed = managed;
        }
        
        private void info(String message) {
            if (disableInfoLogging) {
                return;
            }
            if (sout) {
                System.out.println(message);
            }
            if (logger.isInfoEnabled()) {
                logger.info(message);
            }
        }
        
        public static void main(String[] args) throws Exception {
            ApplicationDeployCommand deployer = new ApplicationDeployCommand();
            if (args.length < 1) {
                System.out.println(deployer.getUsage());
                return;
            }
            
            deployer.deployAndWait(args);
        }

        
        public void deployAndWait(String[] args) {
            
            try {
                parseArgs(args);
                
                final GridServiceManager gsm = waitForGridServiceManager();
                
                File applicationFolder = new File(args[args.length-1]);
                
                long end = System.currentTimeMillis() + deployTimeout;
                
                
                ApplicationConfig applicationConfig = new ApplicationFileDeployment(applicationFolder).create();
                
                // apply security properties to each pu in the application
                for (ProcessingUnitConfigHolder puConfig : applicationConfig.getProcessingUnits()) {
                    if (secured != null) {
                        puConfig.setSecured(secured);
                    }
                    if (userDetails != null) {
                        UserDetailsConfig userDetailsConfig = new UserDetailsConfig();
                        userDetailsConfig.setUsername(userDetails.getUsername());
                        userDetailsConfig.setPassword(userDetails.getPassword());
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
            }
        }

        private void parseArgs(String[] args) {
            CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length - 1);
            String username = null;
            String password = null;
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
                
                if (param.getName().equalsIgnoreCase("deploy-timeout")) {
                    setDeployTimeout(Long.valueOf(param.getArguments()[0]));
                }
                
                if (param.getName().equals("user")) {
                    username = param.getArguments()[0];
                } 
                
                if (param.getName().equals("password")) {
                    password = param.getArguments()[0];
                }
                
                if (param.getName().equals("secured")) {
                    if (param.getArguments().length == 0) {
                        setSecured(true);
                    } else {
                        setSecured(Boolean.parseBoolean(param.getArguments()[0]));
                    }
                }
            }
            
            if (username != null && password != null) {
                setUserDetails(username, password);
            }
           
        }


        private GridServiceManager waitForGridServiceManager() throws TimeoutException {
            
            final Admin admin = createAdmin();
            final GridServiceManager gsm = admin.getGridServiceManagers().waitForAtLeastOne(lookupTimeout,TimeUnit.MILLISECONDS);
            if (gsm == null) {
                throw new TimeoutException("GSM discovery timed out.");
            }
            return gsm;
        }

        private Admin createAdmin() {
            AdminFactory adminFactory = new AdminFactory().useDaemonThreads(true);
            
            if (locators != null) {
                adminFactory.addLocators(locators);
            }
            
            if (groups != null) {
                for (String group : groups) {
                    adminFactory.addGroup(group);
                }
            }
            
            if (userDetails != null) {
                adminFactory.userDetails(userDetails);
            }
            
            final Admin admin = adminFactory.create();
            return admin;
        }

        
        public String getUsage() {
            StringBuilder sb = new StringBuilder();
            if (!managed) {
                sb.append("Usage: ApplicationDeployCommand [-groups groups] [-locators hots1 hots2] [-timeout timeoutValue] [-user xxx -password yyy] [-secured true/false] Application_DirOrZip");
            } else {
                sb.append("Usage: deploy-application [-user xxx -password yyy] [-secured true/false] Application_DirOrZip");
            }
            sb.append("\n    Application_DirOrZip: The path to the application direcoty or zip file containing application.xml and the PU jars.");
            if (!managed) {
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
            sb.append("\n1. Deploy data-app");
            sb.append("\n    - Deploys the application in the data-app directory.");
            sb.append("\n2. Deploy data-app.zip");
            sb.append("\n    - Deploys the application in the data-app zip file.");
            return sb.toString();
        }

}
