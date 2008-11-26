package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalTransportInfoProvider extends InternalTransportAware {

    NIODetails getNIODetails() throws RemoteException;

    NIOStatistics getNIOStatistics() throws RemoteException;
}
