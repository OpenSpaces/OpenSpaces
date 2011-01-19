package org.openspaces.admin.internal.admin;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
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

    ScheduledThreadPoolExecutor getScheduler();

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
     * A generic thread pool for network based operations such as creating a new grid service container.
     * @param runnable
     */
    void scheduleAdminOperation(Runnable runnable);

    
    /**
     * Enables a single event loop threading model in which all
     * event listeners and admin state updates are done on the same thread.
     * The underlying assumption is that event listeners do not perform an I/O operation
     * so they won't block the single event thread.
     * Call this method before begin()
     * @return
     */
    void singleThreadedEventListeners();

    
}
