/*
 * @(#)IRemoteJavaProcessAdmin.java   Jul 22, 2007
 *
 * Copyright 2007 GigaSpaces Technologies Inc.
 */
package org.openspaces.test.client.executor;

import java.rmi.RemoteException;

/**
 * This interface provides admin functionality of forkable java process {@link RemoteJavaCommand}.
 * 
 * @author  Igor Goldenberg
 * @since	1.0
 * @see Executor#executeAsync(RemoteJavaCommand, java.io.File)
 * @see RemoteAsyncCommandResult#getProcessAdmin()
 * @see RemoteJavaCommand
 **/
public interface IRemoteJavaProcessAdmin
		extends IJavaProcess
{   
	/**
	 * Checks by remote method call whether forked java process is alive.
	 *  
	 * @return <code>true</code> if forked java process is alive.
	 * @throws RemoteException Connection lost with java process, the process might be already destroyed.
	 * @throws ForkProcessException Failed to fork java process.
	 **/
	public boolean isAlive() throws RemoteException, ForkProcessException;

	/**
	 * Force to kill java process.
	 * @throws RemoteException Connection lost with java process, the process might be already destroyed.
	 */
   public void killVM() throws RemoteException;
   
   /**
    * Creates a dump of the process JVM.
    * @throws RemoteException
    */
   public void dumpVM() throws RemoteException;
}