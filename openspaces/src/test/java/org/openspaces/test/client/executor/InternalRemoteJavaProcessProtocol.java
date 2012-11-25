/*
 * @(#)InternalRemoteJavaProcessProtocol.java   Jul 25, 2007
 *
 * Copyright 2007 GigaSpaces Technologies Inc.
 */
package org.openspaces.test.client.executor;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

/**
 * Internal remote interface provides communication protocol between RemoteJavaCommand and RemoteJavaProcess.<br>
 * {@link #invoke(String, Class[], Object[])} allows to {@link RemoteJavaCommand} to invoke remote method of user-defined
 * {@link RemoteJavaCommand} remote interface. <br>
 * No needs to compile rmic stubs all remote method invocation performs via {@link #invoke(String, Class[], Object[])}.
 * 
 * @author  Igor Goldenberg
 * @since	1.0
 * @see RemoteJavaCommand
 * @see RemoteJavaProcess
 **/
public interface InternalRemoteJavaProcessProtocol
	extends IRemoteJavaProcessAdmin
{	
	/** invokes supplied remote method invocation on {@link RemoteJavaProcess} */
	@SuppressWarnings("rawtypes")
    public Object invoke(String methodName, Class[] paramType, Object[] args)
	  throws RemoteException, InvocationTargetException;
}