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

package org.openspaces.admin;

import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.jini.rio.boot.BootUtil;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;
import com.gigaspaces.logger.GSLogConfigLoader;

/**
 * A factory allowing to create {@link org.openspaces.admin.Admin} instance.
 *
 * <p>Allows to set the Lookup Service Groups and Locators. The Admin listens for events
 * from the lookup service of components added and removed. It will monitor all components
 * that work within the specified group and locator.
 *
 * <p>Allows to set the username and password that will be used to authenticate when secured
 * services are discovered.
 *
 * @author kimchy
 */
public class AdminFactory {

    private final DefaultAdmin admin = new DefaultAdmin();

    private boolean useGsLogging = true;

    public AdminFactory useGsLogging(boolean useGsLogging) {
        this.useGsLogging = useGsLogging;
        return this;
    }

    /**
     * Adds a lookup group that will be used to find Lookup Services (using multicast)
     * that will be used to listen for events.
     */
    public AdminFactory addGroup(String group) {
        admin.addGroup(group);
        return this;
    }

    /**
     * Adds a comma delimited string of groups that will be used to find the Lookup Services
     * (using multicast).
     */
    public AdminFactory addGroups(String groups) {
        String[] groupsArr = BootUtil.toArray(groups);
        for (String group : groupsArr) {
            addGroup(group);
        }
        return this;
    }

    /**
     * Adds a lookup locator (ip and port) of the lookup service that will be used to listen
     * for events.
     */
    public AdminFactory addLocator(String locator) {
        admin.addLocator(locator);
        return this;
    }

    /**
     * Adds a comma delimited lookup locators (ip and port) of the lookup service that will
     * be used to listen for events.
     */
    public AdminFactory addLocators(String locators) {
        // its already a comma delimited string, so we just add it
        admin.addLocator(locators);
        return this;
    }

    /**
     * Sets the username and password for discovery of secured services.
     */
    public AdminFactory userDetails(String userName, String password) {
        admin.setUserDetails(new User(userName, password));
        return this;
    }

    /**
     * Sets the user details that will be used when discovering secured services.
     */
    public AdminFactory userDetails(UserDetails userDetails) {
        admin.setUserDetails(userDetails);
        return this;
    }

    /**
     * Enables discovery of unmanaged spaces (spaces that are not started by being deployed
     * within the Service Grid). Defaults to <code>false</code> (unmanaged spaces are not discovered).
     */
    public AdminFactory discoverUnmanagedSpaces() {
        admin.getDiscoveryService().discoverUnmanagedSpaces();
        return this;
    }
    
    /**
     * Creates the admin and begins its listening for events from the lookup service.
     */
    public Admin create() {
        if (useGsLogging) {
            GSLogConfigLoader.getLoader();
        }
        admin.begin();
        return admin;
    }
    
    /**
     * Creates the admin and begins its listening for events from the lookup service.
     * @see {@link #create()}
     */
    public Admin createAdmin() {
        return create();
    }

    protected Admin getAdmin() {
        return this.admin;
    }
}
