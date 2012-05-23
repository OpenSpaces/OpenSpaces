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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.pu.container.support.CommandLineParser;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;


/**
 * @author itaif
 * @since 9.0.1
 */
public abstract class AbstractApplicationCommand {

        private final Log logger = LogFactory.getLog(this.getClass());

        private String[] groups;

        public Boolean getSecured() {
            return secured;
        }

        public void setSecured(Boolean secured) {
            this.secured = secured;
        }

        protected Log getLogger() {
            return logger;
        }

        protected String[] getGroups() {
            return groups;
        }

        protected String getLocators() {
            return locators;
        }

        protected int getLookupTimeout() {
            return lookupTimeout;
        }

        protected long getTimeout() {
            return timeout;
        }

        protected boolean isSout() {
            return sout;
        }

        protected boolean isDisableInfoLogging() {
            return disableInfoLogging;
        }

        protected UserDetails getUserDetails() {
            return userDetails;
        }

        protected boolean getManaged() {
            return managed;
        }

        private String locators;

        private int lookupTimeout = 5000;
        
        private long timeout = Long.MAX_VALUE;

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
        
        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
        
        public void setManaged(boolean managed) {
            this.managed = managed;
        }
        
        protected void info(String message) {
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
                
        
        protected void parseArgs(String[] args) {
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
                
                if (param.getName().equalsIgnoreCase("deploy-timeout") || 
                    param.getName().equalsIgnoreCase("undeploy-timeout")) {
                    setTimeout(Long.valueOf(param.getArguments()[0]));
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


        protected GridServiceManager waitForGridServiceManager() throws TimeoutException {
            
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
}
