package org.openspaces.remoting;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openspaces.core.GigaSpace;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteAccessor;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public class SpaceRemotingProxyFactoryBean extends RemoteAccessor implements FactoryBean, InitializingBean, MethodInterceptor {

    private GigaSpace gigaSpace;

    private long timeout = 5000;

    private RemoteRoutingHandler remoteRoutingHandler;

    private boolean globalOneWay = false;

    private boolean voidOneWay = false;

    private Object serviceProxy;

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setRemoteRoutingHandler(RemoteRoutingHandler remoteRoutingHandler) {
        this.remoteRoutingHandler = remoteRoutingHandler;
    }

    public void setGlobalOneWay(boolean globalOneWay) {
        this.globalOneWay = globalOneWay;
    }

    public void setVoidOneWay(boolean voidOneWay) {
        this.voidOneWay = voidOneWay;
    }

    public void afterPropertiesSet() {
        Assert.notNull(getServiceInterface(), "serviceInterface property is required");
        Assert.notNull(gigaSpace, "gigaSpace property is required");
        this.serviceProxy = ProxyFactory.getProxy(getServiceInterface(), this);
    }

    public Object getObject() {
        return this.serviceProxy;
    }

    public Class getObjectType() {
        return getServiceInterface();
    }

    public boolean isSingleton() {
        return true;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        SpaceRemoteInvocation remoteInvocation = new SpaceRemoteInvocation(getServiceInterface().getName(),
                methodInvocation.getMethod().getName(), methodInvocation.getArguments());

        if (remoteRoutingHandler != null) {
            remoteRoutingHandler.setRemoteInvocationRouting(remoteInvocation);
        }
        if (remoteInvocation.getRouting() == null) {
            remoteInvocation.setRouting(new Integer(remoteInvocation.hashCode()));
        }
        if (globalOneWay) {
            remoteInvocation.oneWay = Boolean.TRUE;
        } else {
            if (voidOneWay && methodInvocation.getMethod().getReturnType() == void.class) {
                remoteInvocation.oneWay = Boolean.TRUE;
            }
        }

        gigaSpace.write(remoteInvocation);

        Object value = gigaSpace.take(new SpaceRemoteResult(remoteInvocation), timeout);
        SpaceRemoteResult invokeResult = (SpaceRemoteResult) value;
        if (invokeResult == null) {
            throw new SpaceRemotingTimeoutException("Timeout waiting for result with invocation [" + remoteInvocation + "]", timeout);
        }
        if (invokeResult.getEx() != null) {
            throw invokeResult.getEx();
        }
        return invokeResult.getResult();
    }
}