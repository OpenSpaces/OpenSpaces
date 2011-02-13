package org.openspaces.itest.remoting.methodannotations;

import org.aopalliance.intercept.MethodInvocation;
import org.openspaces.remoting.RemoteInvocationAspect;
import org.openspaces.remoting.RemotingInvoker;

/**
 * @author uri
 */
public class ReturnTrueRemoteInvocationAspect implements RemoteInvocationAspect<Boolean> {
    public Boolean invoke(MethodInvocation methodInvocation, RemotingInvoker remotingInvoker) throws Throwable {
        return Boolean.TRUE;
    }
}
