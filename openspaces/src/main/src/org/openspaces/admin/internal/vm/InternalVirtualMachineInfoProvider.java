package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.VirtualMachineConfiguration;
import com.gigaspaces.jvm.VirtualMachineStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalVirtualMachineInfoProvider extends InternalVirtualMachineAware {

    VirtualMachineConfiguration getVirtualMachineConfiguration() throws RemoteException;

    VirtualMachineStatistics getVirtualMachineStatistics() throws RemoteException;
}