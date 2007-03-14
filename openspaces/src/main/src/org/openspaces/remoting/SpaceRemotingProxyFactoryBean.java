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
 * A space remoting proxy that forware the service execution to a remote service with the space as
 * the transport layer. Services are remotly exported in the "server side" using the
 * {@link org.openspaces.remoting.SpaceRemotingServiceExporter}. This proxy builds a representation
 * of the remote invocation using {@link org.openspaces.remoting.SpaceRemoteInvocation} and waits
 * for a remoting response represented by {@link org.openspaces.remoting.SpaceRemoteResult}.
 * 
 * <p>
 * The proxy requires a {@link #setGigaSpace(org.openspaces.core.GigaSpace)} interface to be set in
 * order to write the remote invocation and wait for a response using the space API. It also
 * requires a {@link #setServiceInterface(Class)} which represents the interface that will be
 * proxied.
 * 
 * <p>
 * Allows for one way invocations (i.e. not waiting for a response). The one way invocation can be
 * set globablly for all of the service methods by setting {@link #setGlobalOneWay(boolean)} or can
 * be enabled only for methods that return <code>void</code> by setting
 * {@link #setVoidOneWay(boolean)}. Note, if using one way invocation and an exception is raised by
 * the remote service, it won't be raised by this proxy.
 * 
 * <p>
 * A timeout which controls how long the proxy will wait for the response can be set using
 * {@link #setTimeout(long)}. The timeout value if in <b>milliseconds</b>.
 * 
 * <p>
 * The space remote proxy supports a future based invocation. This means that if, on the clien side,
 * one of the service interface methods returns {@link org.openspaces.remoting.RemoteFuture}, it
 * can be used for async execution. Note, this means that in terms of interfaces there will have to
 * be two different service interfaces (under the same package and with the same name). One for the
 * server side service that returns the actual value, and one on the client side that for the same
 * method simply returns the future.
 * 
 * <p>
 * In case of remote invocation over a partitioned space the default partitioned routing index will
 * be random (the hashCode of the newly created
 * {@link org.openspaces.remoting.SpaceRemoteInvocation} class). The proxy allows for a pluggable
 * routing handler implementation by setting {@link #setRemoteRoutingHandler(RemoteRoutingHandler)}.
 * 
 * @author kimchy
 * @see org.openspaces.remoting.SpaceRemotingServiceExporter
 */
public class SpaceRemotingProxyFactoryBean extends RemoteAccessor implements FactoryBean, InitializingBean,
        MethodInterceptor {

    private GigaSpace gigaSpace;

    private long timeout = 5000;

    private RemoteRoutingHandler remoteRoutingHandler;

    private boolean globalOneWay = false;

    private boolean voidOneWay = false;

    private Object serviceProxy;

    /**
     * Sets the GigaSpace interface that will be used to work with the space as the transport layer
     * for both {@link org.openspaces.remoting.SpaceRemoteInvocation} and
     * {@link org.openspaces.remoting.SpaceRemoteResult}.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * Sets the timeout that will be used to wait for the remote inovocation response. The timeout
     * value is in <b>milliseconds</b> and defaults to <code>5000</code> (5 seconds).
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * In case of remote invocation over a partitioned space the default partitioned routing index
     * will be random (the hashCode of the newly created
     * {@link org.openspaces.remoting.SpaceRemoteInvocation} class). This
     * {@link org.openspaces.remoting.RemoteRoutingHandler} allows for custom routing computation
     * (for example, based on one of the service method parameters).
     */
    public void setRemoteRoutingHandler(RemoteRoutingHandler remoteRoutingHandler) {
        this.remoteRoutingHandler = remoteRoutingHandler;
    }

    /**
     * If set to <code>true</code> (defaults to <code>false</code>) all of the service methods
     * will be invoked and the proxy will not wait for a return value. Note, any exception raised by
     * the remote service will be logged on the server side and not propagated to the client.
     */
    public void setGlobalOneWay(boolean globalOneWay) {
        this.globalOneWay = globalOneWay;
    }

    /**
     * If set to <code>true</code> (defaults to <code>false</code>) service methods that return
     * void will be invoked and the proxy will not wait for a return value. Note, any exception
     * raised by the remote service will be logged on the server side and not propagated to the
     * client.
     */
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

    public Class<?> getObjectType() {
        return getServiceInterface();
    }

    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        SpaceRemoteInvocation remoteInvocation = new SpaceRemoteInvocation(getServiceInterface().getName(),
                methodInvocation.getMethod().getName(), methodInvocation.getArguments());

        if (remoteRoutingHandler != null) {
            remoteRoutingHandler.setRemoteInvocationRouting(remoteInvocation);
        }
        if (remoteInvocation.getRouting() == null) {
            remoteInvocation.setRouting(new Integer(remoteInvocation.hashCode()));
        }
        // check if this invocation will be a one way invocation
        if (globalOneWay) {
            remoteInvocation.oneWay = Boolean.TRUE;
        } else {
            if (voidOneWay && methodInvocation.getMethod().getReturnType() == void.class) {
                remoteInvocation.oneWay = Boolean.TRUE;
            }
        }

        gigaSpace.write(remoteInvocation);

        // if this is a one way invocation, simply return null
        if (remoteInvocation.oneWay != null && remoteInvocation.oneWay.booleanValue()) {
            return null;
        }

        // if the return value is a future, return the future
        if (RemoteFuture.class.isAssignableFrom(methodInvocation.getMethod().getReturnType())) {
            return new DefaultRemoteFuture(gigaSpace, remoteInvocation);
        }

        Object value = gigaSpace.take(new SpaceRemoteResult(remoteInvocation), timeout);
        SpaceRemoteResult invokeResult = (SpaceRemoteResult) value;
        if (invokeResult == null) {
            throw new SpaceRemotingTimeoutException("Timeout waiting for result with invocation [" + remoteInvocation
                    + "]", timeout);
        }
        if (invokeResult.getEx() != null) {
            throw invokeResult.getEx();
        }
        return invokeResult.getResult();
    }
}