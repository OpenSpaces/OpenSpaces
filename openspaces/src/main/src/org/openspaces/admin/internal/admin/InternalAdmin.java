package org.openspaces.admin.internal.admin;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.operatingsystem.OSDetails;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalAdmin extends Admin {

    void addLookupService(InternalLookupService lookupService, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails);

    void removeLookupService(String uid);

    void addGridServiceManager(InternalGridServiceManager gridServiceManager, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails);

    void removeGridServiceManager(String uid);

    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails);

    void removeGridServiceContainer(String uid);

    void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);
}
