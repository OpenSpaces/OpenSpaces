package org.openspaces.admin.internal.admin.transport;

import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalTransportInfoProvider extends InternalTransportAware {

    TransportConfiguration getTransportConfiguration() throws RemoteException;

    TransportStatistics getTransportStatistics() throws RemoteException;
}
