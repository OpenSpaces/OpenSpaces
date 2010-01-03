package org.openspaces.admin.internal.admin;

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

    void addLookupService(InternalLookupService lookupService, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeLookupService(String uid);

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeGridServiceAgent(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeGridServiceManager(String uid);

    void addElasticServiceManager(InternalElasticServiceManager elasticServiceManager, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeElasticServiceManager(String uid);
    
    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeGridServiceContainer(String uid);

    void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeProcessingUnitInstance(String uid, boolean removeEmbeddedSpaces);

    void addSpaceInstance(InternalSpaceInstance spaceInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones);

    void removeSpaceInstance(String uid);
}
