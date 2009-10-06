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

import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.log.LogEntryMatcher;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;

import java.util.concurrent.TimeUnit;

/**
 * A Grid Service Agent is a process manager allowing to start and stop (on the operating system process level)
 * processes (such as {@link org.openspaces.admin.gsm.GridServiceManager}, {@link org.openspaces.admin.gsc.GridServiceContainer}
 * and {@link org.openspaces.admin.lus.LookupService}. It can also globally manage certain process type (a process type
 * is a GSM for example), which means that when running X number of Grid Service Agents, at least N processes will
 * run between them.
 *
 * @author kimchy
 */
public interface GridServiceAgent extends GridComponent, LogProviderGridComponent {

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
    void startGridService(GridServiceOptions options);

    LogEntries log(final LogProcessType type, final long pid, LogEntryMatcher matcher);

    LogEntries[] log(final LogProcessType type, LogEntryMatcher matcher);
}