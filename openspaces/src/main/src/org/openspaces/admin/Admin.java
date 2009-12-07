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

import java.util.concurrent.TimeUnit;

import net.jini.core.discovery.LookupLocator;

import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.zone.Zones;
import org.openspaces.admin.dump.DumpProvider;

/**
 * The main interface for accessing Admin API. Created using the {@link org.openspaces.admin.AdminFactory}
 * class.
 *
 * <p>Provides access to the main elements in the Admin API and in GigaSpaces such as the
 * {@link org.openspaces.admin.gsa.GridServiceAgents}, {@link org.openspaces.admin.lus.LookupServices},
 * {@link org.openspaces.admin.gsm.GridServiceManagers}, {@link org.openspaces.admin.gsc.GridServiceContainers},
 * {@link org.openspaces.admin.pu.ProcessingUnits}, and {@link org.openspaces.admin.space.Spaces}.
 *
 * <p>Also allows to change monitoring (not statistics) interval (works in a polling fashion) of state changing
 * elements such as the processing unit, the Grid Service Agent, and the Space.
 *
 * <p>Implements the {@link org.openspaces.admin.StatisticsMonitor} interface, allowing in one single
 * call ({@link #startStatisticsMonitor()}) to start statistics monitors on all the elements it manages (such as
 * the {@link org.openspaces.admin.space.Spaces}, and {@link org.openspaces.admin.vm.VirtualMachines}.
 *
 * <p>Provides one stop shop for registering all event listeners that extend {@link org.openspaces.admin.AdminEventListener}
 * using the {@link #addEventListener(AdminEventListener)} and their removal {@link #removeEventListener(AdminEventListener)}.
 * The actual event listener interfaces will be automatically detected and added to the correct component.
 *
 * @author kimchy
 */
public interface Admin extends StatisticsMonitor, DumpProvider {

    /**
     * Returns the lookup groups this admin uses.
     *
     * @see org.openspaces.admin.AdminFactory#addGroup(String)
     */
    String[] getGroups();

    /**
     * Returns the lookup locators this admin uses.
     *
     * @see org.openspaces.admin.AdminFactory#addLocator(String)
     */
    LookupLocator[] getLocators();

    /**
     * Sets the processing unit monitor (not statistics) interval. The monitor basically
     * checks the status of each processing unit ({@link org.openspaces.admin.pu.ProcessingUnit#getStatus()}
     * among other things.
     *
     * <p>Defaults to 1 second.
     *
     * @param interval The interval to use.
     * @param timeUnit The time unit the interval is at.
     */
    void setProcessingUnitMonitorInterval(long interval, TimeUnit timeUnit);

    /**
     * Sets the Grid Service Agent processes monitor (not statistics) interval. The monitor
     * basically updates the list of processes the agent manages ({@link org.openspaces.admin.gsa.GridServiceAgent#getProcessesDetails()}.
     *
     * <p>Defaults to 5 seconds.
     *
     * @param interval The interval to use.
     * @param timeUnit The time unit the interval is at.
     */
    void setAgentProcessessMonitorInterval(long interval, TimeUnit timeUnit);

    /**
     * Sets the Space monitor (not statistics) interval. The monitor checks each space instance and updates
     * its different status (such as the replication status).
     *
     * <p>Defaults to 1 second.
     *
     * @param interval The interval to use.
     * @param timeUnit The time unit the interval is at.
     */
    void setSpaceMonitorInterval(long interval, TimeUnit timeUnit);

    /**
     * The Admin API uses a shared scheduler thread pool to perform *all* scheduled operations (monitoring,
     * statistics monitoring). The number of threads used can be set here.
     *
     * <p>Defaults to 10 threads.
     *
     * @param coreThreads The number of threads the shared scheduler thread pool will use,
     */
    void setSchedulerCorePoolSize(int coreThreads);

    /**
     * The default timeout to be used for operations that have a wait for mechanism, i.e. operations
     * with overloaded methods containing <tt>(..., long timeout, TimeUnit timeUnit)</tt>. If not set, the
     * default is <tt>Long.MAX_VALUE, TimeUnit.MILLISECONDS</tt>
     * 
     * @param timeout The timeout value to be used as the default operation timeout if non was provided.
     * @param timeUnit The time-unit corresponding to the timeout.
     */
    void setDefaultTimeout(long timeout, TimeUnit timeUnit);
    
    /**
     * Closes the Admin, releasing any resource and stops listening for events from the lookup service.
     */
    void close();

    /**
     * Returns the grid service agents discovered.
     */
    GridServiceAgents getGridServiceAgents();

    /**
     * Returns the lookup services discovered.
     */
    LookupServices getLookupServices();

    /**
     * Returns the grid service managers discovered.
     */
    GridServiceManagers getGridServiceManagers();

    /**
     * Returns the grid service containers discovered.
     */
    GridServiceContainers getGridServiceContainers();

    /**
     * Returns the Grid Component represented by this UID; e.g. GSA, LUS, GSM, GSC, etc.
     * @see GridComponent#getUid()
     * 
     * @param uid The UID of the service returned by {@link GridComponent#getUid()};
     *            <code>null</code> if no representation.
     * @return The Grid Component represented by this UID.
     */
    GridComponent getGridComponentByUID(String uid);
    
    /**
     * Returns the machines discovered.
     */
    Machines getMachines();

    /**
     * Returns the zones discovered.
     */
    Zones getZones();

    /**
     * Returns the transports discovered.
     */
    Transports getTransports();

    /**
     * Returns the Virtual Machines discovered.
     */
    VirtualMachines getVirtualMachines();

    /**
     * Returns the Operating Systems discovered.
     */
    OperatingSystems getOperatingSystems();

    /**
     * Returns the Processing Units discovered.
     */
    ProcessingUnits getProcessingUnits();

    /**
     * Returns the spaces discovered.
     */
    Spaces getSpaces();

    /**
     * Smart addition of event listeners. Will automatically add to the correct place any interface
     * that extends the {@link org.openspaces.admin.AdminEventListener} interface.
     *
     * @see #removeEventListener(AdminEventListener)
     */
    void addEventListener(AdminEventListener eventListener);

    /**
     * Smart removal of event listeners. Will automatically remove to the correct place any interface
     * that extends the {@link org.openspaces.admin.AdminEventListener} interface.
     *
     * @see #addEventListener(AdminEventListener)
     */
    void removeEventListener(AdminEventListener eventListener);
}
