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

package org.openspaces.admin.gsa;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;

import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.log.CompoundLogEntries;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;

/**
 * A Grid Service Agent is a process manager allowing to start and stop (on the operating system process level)
 * processes (such as {@link org.openspaces.admin.gsm.GridServiceManager}, {@link org.openspaces.admin.gsc.GridServiceContainer}
 * and {@link org.openspaces.admin.lus.LookupService}. It can also globally manage certain process type (a process type
 * is a GSM for example), which means that when running X number of Grid Service Agents, at least N processes will
 * run between them.
 *
 * @author kimchy
 */
public interface GridServiceAgent extends GridComponent, LogProviderGridComponent, DumpProvider {

    /**
     * Returns all the processes details this agent is currently running.
     */
    AgentProcessesDetails getProcessesDetails();

    /**
     * Starts a {@link org.openspaces.admin.gsm.GridServiceManager} based on the provided options.
     */
    void startGridService(GridServiceManagerOptions options);

    /**
     * Starts a {@link org.openspaces.admin.gsm.GridServiceManager} based on the provided options and waits
     * indefinitely until it is discovered by the admin, which is then returned.
     */
    GridServiceManager startGridServiceAndWait(GridServiceManagerOptions options);

    /**
     * Starts a {@link org.openspaces.admin.gsm.GridServiceManager} based on the provided options and waits
     * for the given timeout (in time unit) until it is discovered by the admin, which is then returned.
     */
    GridServiceManager startGridServiceAndWait(GridServiceManagerOptions options, long timeout, TimeUnit timeUnit);

    /**
     * Starts a {@link org.openspaces.admin.esm.ElasticServiceManager} based on the provided options.
     */
    void startGridService(ElasticServiceManagerOptions options);

    /**
     * Starts a {@link org.openspaces.admin.esm.ElasticServiceManager} based on the provided options and waits
     * indefinitely until it is discovered by the admin, which is then returned.
     */
    ElasticServiceManager startGridServiceAndWait(ElasticServiceManagerOptions options);

    /**
     * Starts a {@link org.openspaces.admin.esm.ElasticServiceManager} based on the provided options and waits
     * for the given timeout (in time unit) until it is discovered by the admin, which is then returned.
     */
    ElasticServiceManager startGridServiceAndWait(ElasticServiceManagerOptions options, long timeout, TimeUnit timeUnit);

    /**
     * Starts a {@link org.openspaces.admin.gsc.GridServiceContainer} based on the provided options.
     */
    void startGridService(GridServiceContainerOptions options);

    /**
     * Starts a {@link org.openspaces.admin.gsc.GridServiceContainer} based on the provided options and waits
     * indefinitely until it is discovered by the admin, which is then returned.
     */
    GridServiceContainer startGridServiceAndWait(GridServiceContainerOptions options);

    /**
     * Starts a {@link org.openspaces.admin.gsc.GridServiceContainer} based on the provided options and waits
     * for the given timeout (in time unit) until it is discovered by the admin, which is then returned.
     */
    GridServiceContainer startGridServiceAndWait(GridServiceContainerOptions options, long timeout, TimeUnit timeUnit);

    /**
     * Starts a {@link org.openspaces.admin.lus.LookupService} based on the provided options.
     */
    void startGridService(LookupServiceOptions options);

    /**
     * Starts a {@link org.openspaces.admin.lus.LookupService} based on the provided options and waits
     * indefinitely until it is discovered by the admin, which is then returned.
     */
    LookupService startGridServiceAndWait(LookupServiceOptions options);

    /**
     * Starts a {@link org.openspaces.admin.lus.LookupService} based on the provided options and waits
     * for the given timeout (in time unit) until it is discovered by the admin, which is then returned.
     */
    LookupService startGridServiceAndWait(LookupServiceOptions options, long timeout, TimeUnit timeUnit);

    /**
     * Starts a generic process of a given type.
     */
    int startGridService(GridServiceOptions options);

    /**
     * Kills a process based on the agent id provided to it.
     */
    void killByAgentId(int agentId);

    /**
     * Extract the log entries matching the provided matcher for the process type and process id.
     */
    LogEntries logEntries(final LogProcessType type, final long pid, LogEntryMatcher matcher);

    /**
     * Extracts all the log entries of all the "live" runtime components that this agent is running
     * matching the given matcher.
     */
    CompoundLogEntries liveLogEntries(LogEntryMatcher matcher);

    /**
     * Extracts all the log entries for the provided process type including both "live" runtime
     * components and ones that are no longer running. 
     */
    CompoundLogEntries allLogEntries(final LogProcessType type, LogEntryMatcher matcher);

    /**
     * Shuts down the GSA.
     * @since 7.1.2
     */
    void shutdown();
    
    /**
     * @return true if this agent is in admin.getGridServiceAgents()
     * @since 8.0
     */
    boolean isRunning();
}