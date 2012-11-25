package org.openspaces.test.client.executor;

import java.rmi.RemoteException;

/**
 * Remote interface for invoking dump operations on remote processes mainly.
 * @author Moran Avigdor
 */
public interface IRemoteDumpProvider {
	public void generateDump() throws RemoteException;

    public void generateHeapDump(String spaceName, String testDirPath) throws RemoteException;
}
