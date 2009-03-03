package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalVirtualMachineInfoProvider extends InternalVirtualMachineAware {

    JVMDetails getJVMDetails() throws RemoteException;

    JVMStatistics getJVMStatistics() throws RemoteException;

    void runGc() throws RemoteException;
}