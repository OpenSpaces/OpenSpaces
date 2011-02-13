package org.openspaces.itest.remoting.methodannotations.executor;

import org.openspaces.remoting.ServiceExecutionAspect;
import org.openspaces.remoting.SpaceRemotingInvocation;

import java.lang.reflect.InvocationTargetException;

/**
 * @author uri
 */
public class ReturnFirstMetaArgumentExecutionAspect implements ServiceExecutionAspect{
    public Object invoke(SpaceRemotingInvocation invocation, MethodInvocation method, Object service) throws InvocationTargetException, IllegalAccessException {
        Object[] metaArguments = invocation.getMetaArguments();
        if (metaArguments != null) {
            return metaArguments[0];
        } else {
            return method.invoke(service, invocation.getArguments());
        }
    }
}
