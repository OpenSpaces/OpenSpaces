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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jini.rio.boot.BootUtil;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.security.AdminFilter;

import com.gigaspaces.logger.GSLogConfigLoader;
import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;

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

    private boolean useGsLogging = true;

    private final AtomicBoolean created = new AtomicBoolean(false);

    private boolean useDaemonThreads = false;
    private boolean singleThreadedEventListeners = false;
    private final List<String> groups = new ArrayList<String>();
    private final List<String> locators = new ArrayList<String>();
    private UserDetails userDetails = null;
    private boolean discoverUnmanagedSpaces = false;
    private AdminFilter adminFilter;
    
    /**
     * By default the Admin finds in classpath the GS logging config file. This file is used for the setting
     * GS logging configuration. This file is searched under /config/gs_logging.properties
     * User can set system property: -Djava.util.logging.config.file to use his own java logging configuration.
     * GS logger will not overwrite it.
     * 
     * Call this method with false to disable loading of gs_logging.properties by the admin API
     */
    public AdminFactory useGsLogging(boolean useGsLogging) {
        this.useGsLogging = useGsLogging;
        return this;
    }

    /**
     * For backwards comparability reasons, all Admin worker threads are not daemon threads.
     * That means that creating a new admin object without closing it will leave non-daemon threads, 
     * which will prevent the JVM from shutting down.
     * It is recommended to call this method with true in order to set all worker threads as daemon threads. 
     */
    public AdminFactory useDaemonThreads(boolean useDaemonThreads) {
        this.useDaemonThreads = useDaemonThreads;
        return this;
    }

    /**
     * Adds a lookup group that will be used to find Lookup Services (using multicast)
     * that will be used to listen for events.
     */
    public AdminFactory addGroup(String group) {
        groups.add(group);
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
        locators.add(locator);
        return this;
    }

    /**
     * Adds a comma delimited lookup locators (ip and port) of the lookup service that will
     * be used to listen for events.
     */
    public AdminFactory addLocators(String locators) {
        String[] locatorsArr = BootUtil.toArray(locators);
        for (String locator : locatorsArr) {
            addLocator(locator);
        }
        return this;
    }

    /**
     * Sets the username and password for discovery of secured services.
     */
    public AdminFactory userDetails(String userName, String password) {
        return userDetails(new User(userName, password));
    }

    /**
     * Sets the user details that will be used when discovering secured services.
     */
    public AdminFactory userDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
        return this;
    }

    /***
     * Sets adminFilter, allows to filter services, machines and etc.
     * @param adminFilter
     * @since 9.1.1
     */
    public AdminFactory adminFilter( AdminFilter adminFilter ){
        this.adminFilter = adminFilter;
        return this;
    }
    
    /**
     * Enables discovery of unmanaged spaces (spaces that are not started by being deployed
     * within the Service Grid). Defaults to <code>false</code> (unmanaged spaces are not discovered).
     */
    public AdminFactory discoverUnmanagedSpaces() {
        this.discoverUnmanagedSpaces = true;
        return this;
    }

    //exposed by InternalAdminFactory
    protected AdminFactory singleThreadedEventListeners() {
        this.singleThreadedEventListeners = true;
        return this;
    }
    
    /**
     * Creates the admin and begins its listening for events from the lookup service.
     */
    public Admin create() {
        if (!created.compareAndSet(false, true)) {
            //used to make sure that the admin#begin() is not called twice
            throw new IllegalStateException("AdminFactory#create() has already been called.");
        }
        if (useGsLogging) {
            GSLogConfigLoader.getLoader();
        }
        DefaultAdmin admin = new DefaultAdmin(useDaemonThreads, singleThreadedEventListeners);
        admin.setUserDetails(userDetails);
        admin.setAdminFilter(adminFilter);
        for (String group : groups) {
            admin.addGroup(group);
        }
        for (String locator : locators) {
            admin.addLocator(locator);
        }
        if (this.discoverUnmanagedSpaces) {
            admin.getDiscoveryService().discoverUnmanagedSpaces();
        }
        admin.begin();
        return admin;
    }
    
    /**
     * Creates the admin and begins its listening for events from the lookup service.
     * @see #create()
     */
    public Admin createAdmin() {
        return create();
    }
}
