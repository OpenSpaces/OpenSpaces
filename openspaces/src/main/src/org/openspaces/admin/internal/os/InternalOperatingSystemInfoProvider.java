package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OperatingSystemConfiguration;
import com.gigaspaces.operatingsystem.OperatingSystemStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalOperatingSystemInfoProvider extends InternalOperatingSystemAware {

    OperatingSystemConfiguration getOperatingSystemConfiguration() throws RemoteException;

    OperatingSystemStatistics getOperatingSystemStatistics() throws RemoteException;
}
