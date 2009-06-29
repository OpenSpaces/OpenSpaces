package org.openspaces.admin.internal.vm;

import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalVirtualMachineInfoProvider extends InternalVirtualMachineAware {

    JVMDetails getJVMDetails() throws RemoteException;

    JVMStatistics getJVMStatistics() throws RemoteException;

    void runGc() throws RemoteException;
}