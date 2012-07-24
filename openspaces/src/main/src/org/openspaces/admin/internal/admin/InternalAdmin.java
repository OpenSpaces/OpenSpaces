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
package org.openspaces.admin.internal.admin;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.DefaultAdmin.ScheduledLeasedCommand;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstance;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.security.directory.UserDetails;

/**
 * @author kimchy
 */
public interface InternalAdmin extends Admin {

    Log getAdminLogger();
    
    ScheduledThreadPoolExecutor getScheduler();
    
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);

    long getScheduledSpaceMonitorInterval();
    
    long getDefaultTimeout();
    TimeUnit getDefaultTimeoutTimeUnit();

    UserDetails getUserDetails();

    void pushEvent(Object listener, Runnable notifier);

    void pushEventAsFirst(Object listener, Runnable notifier);

    void raiseEvent(Object listener, Runnable notifier);

    void addLookupService(InternalLookupService lookupService, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeLookupService(String uid);

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeGridServiceAgent(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeGridServiceManager(String uid);

    void addElasticServiceManager(InternalElasticServiceManager elasticServiceManager, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeElasticServiceManager(String uid);
    
    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeGridServiceContainer(String uid);

    void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeProcessingUnitInstance(String uid, boolean removeEmbeddedSpaces);

    void addSpaceInstance(InternalSpaceInstance spaceInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeSpaceInstance(String uid);

    void assertStateChangesPermitted();
    
    /**
     * Any internal admin objects state change must be scheduled using this method.
     * In case there is a single event loop thread (non blocking event listeners), 
     * the specified runnable is added to the event loop queue.
     * Otherwise it is executed on the calling thread.
     * @param runnable
     */
    void scheduleNonBlockingStateChange(Runnable runnable);
    
    
    /**
     * Any internal admin objects state change based on polling must be scheduled using this method.
     * In case there is a single event loop thread (non blocking event listeners), 
     * the specified command is added to the event loop queue.
     * Otherwise it is executed on the admin scheduler thread pool.
     * @param runnable
     */
    
    ScheduledFuture<?> scheduleWithFixedDelayNonBlockingStateChange(
            Runnable command, 
            long initialDelay,  
            long delay, 
            TimeUnit unit);

    /**
     * Any internal delayed admin objects state change based on polling must be scheduled using this method.
     * In case there is a single event loop thread (non blocking event listeners), 
     * the specified command is added to the event loop queue.
     * Otherwise it is executed on the admin scheduler thread pool.
     * @param runnable
     */

    ScheduledFuture<?> scheduleOneTimeWithDelayNonBlockingStateChange(
            final Runnable command, 
            long delay, TimeUnit unit);

    /**
     * A generic thread pool for network based operations such as creating a new grid service container.
     * @param runnable
     */
    void scheduleAdminOperation(Runnable runnable);

    /**
     * Schedules a Runnable with a fixed delay until its lease expires. Using the {@link ScheduledLeasedCommand} to renew the lease if necessary.
     * Polling mechanisms can use this method for updating objects for a specified interval (lease) to ensure:
     * 1. On-demand polling only when requested (similar to startStatisticsMonitor and stopStatisticsMonitor methods)
     * 2. polling never continues more than the requested lease (e.g. even if client fails to call stopStatisticsMonitor)
     * 3. polling does not block user thread requesting updated objects
     * 
     * @param command the command to execute each scheduled period
     * @param initialDelay initial delay before executing the command
     * @param delay the delay period between successive executions
     * @param delayUnit the time unit of the initialDelay and delay parameters
     * @param lease the period of time until this command may continue to be scheduled
     * @param leaseUnit the time unit of the lease parameter
     * @return a scheduled leased task which can be renewed or checked for expiration.
     */
    ScheduledLeasedCommand scheduleWithFixedDelayUntilLeaseExpires(Runnable command, long initialDelay, long delay, TimeUnit delayUnit, long lease, TimeUnit leaseUnit);
    
    /**
     * Enables a single event loop threading model in which all
     * event listeners and admin state updates are done on the same thread.
     * The underlying assumption is that event listeners do not perform an I/O operation
     * so they won't block the single event thread.
     * Call this method before begin()
     * @return
     */
    void singleThreadedEventListeners();

    boolean isSingleThreadedEventListeners();

}
