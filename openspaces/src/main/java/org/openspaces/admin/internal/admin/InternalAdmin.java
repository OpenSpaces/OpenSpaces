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

import java.rmi.RemoteException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.security.AdminFilter;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.service.SecuredService;

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

    void login(SecuredService service) throws SecurityException, RemoteException;
    
    AdminFilter getAdminFilter();

    void pushEvent(Object listener, Runnable notifier);

    void pushEventAsFirst(Object listener, Runnable notifier);

    void raiseEvent(Object listener, Runnable notifier);

    void addLookupService(InternalLookupService lookupService, NIODetails nioDetails, 
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeLookupService(String uid);

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent, NIODetails nioDetails, 
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeGridServiceAgent(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager, NIODetails nioDetails, 
            OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones, boolean acceptVM);

    void removeGridServiceManager(String uid);

    void addElasticServiceManager(InternalElasticServiceManager elasticServiceManager, 
            NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, 
            String[] zones, boolean acceptVM);

    void removeElasticServiceManager(String uid);
    
    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeGridServiceContainer(String uid);

    void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeProcessingUnitInstance(String uid, boolean removeEmbeddedSpaces);

    void addSpaceInstance(InternalSpaceInstance spaceInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String jmxUrl, String[] zones);

    void removeSpaceInstance(String uid);

    void assertStateChangesPermitted();
    
    void addGatewayProcessingUnit( GatewayProcessingUnit gatewayProcessingUnit );
    
    void removeGatewayProcessingUnit( String uid );
    
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

    boolean isSingleThreadedEventListeners();

}
