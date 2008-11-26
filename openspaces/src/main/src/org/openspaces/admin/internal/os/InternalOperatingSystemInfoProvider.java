package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalOperatingSystemInfoProvider extends InternalOperatingSystemAware {

    OSDetails getOSDetails() throws RemoteException;

    OSStatistics getOSStatistics() throws RemoteException;
}
