/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.remoting;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openspaces.core.GigaSpace;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteAccessor;
import org.springframework.util.Assert;

/**
 * A space <b>sync</b> remoting proxy that forward the service execution to a remote service with the space as
 * the transport layer. Services are remotely exported in the "server side" using the
 * {@link SpaceRemotingServiceExporter}. This proxy uses the fact that the service exporter is defined as a filter
 * within the remote Space, causing the call to be sync. Either a <code>takeMultiple</code> (in case of an
 * invocation with a result) or <code>readMultiple</code> (in case the invocation is one way) is invoked on the
 * Space using a template ({@link org.openspaces.remoting.SyncSpaceRemotingEntry}) that actually holds the invocation
 * information. The service exporter traps the operation (in the "before" stage) using the filter and writes the
 * result to the Space which is then read by the actual readMultuple/takeMultiple.
 *
 * <p>The proxy requires a {@link #setGigaSpace(org.openspaces.core.GigaSpace)} interface to be set in
 * order to write execute.
 *
 * <p>Allows for one way invocations (i.e. not waiting for a response). The one way invocation can be
 * set globally for all of the service methods by setting {@link #setGlobalOneWay(boolean)} or can
 * be enabled only for methods that return <code>void</code> by setting
 * {@link #setVoidOneWay(boolean)}. Note, if using one way invocation and an exception is raised by
 * the remote service, it won't be raised by this proxy.
 *
 * <p>In case of remote invocation over a partitioned space the default partitioned routing index will
 * be random (the hashCode of the newly created {@link org.openspaces.remoting.SyncSpaceRemotingEntry} class).
 * The proxy allows for a pluggable routing handler implementation by setting
 * {@link #setRemoteRoutingHandler(RemoteRoutingHandler)}.
 *
 * <p>The proxy allows to perform broadcast the remote invocation to all different cluster members (partitions
 * for example) by setting the {@link #setBroadcast(boolean) broadcast} flag to <code>true</code>/ In such cases,
 * a custom {@link #setRemoteResultReducer(RemoteResultReducer)}  can be plugged to reduce the results of
 * all different services into a single response (assuming that the service has a return value).
 *
 * @author kimchy
 * @see org.openspaces.remoting.SpaceRemotingServiceExporter
 */
public class SyncSpaceRemotingProxyFactoryBean extends RemoteAccessor implements FactoryBean, InitializingBean,
        MethodInterceptor {

    private GigaSpace gigaSpace;

    private RemoteRoutingHandler remoteRoutingHandler;

    private boolean globalOneWay = false;

    private boolean voidOneWay = false;

    private boolean broadcast = false;

    private boolean returnFirstResult = true;

    private RemoteResultReducer remoteResultReducer;

    private Object serviceProxy;

    /**
     * Sets the GigaSpace interface that will be used to work with the space as the transport layer.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * In case of remote invocation over a partitioned space the default partitioned routing index
     * will be random (the hashCode of the newly created
     * {@link org.openspaces.remoting.SyncSpaceRemotingEntry} class). This
     * {@link RemoteRoutingHandler} allows for custom routing computation
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

    /**
     * If set the <code>true</code> (defaults to <code>false</code>) causes the remote invocation
     * to be called on all active (primary) cluster memebers.
     */
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    /**
     * When using broadcast set to <code>true</code>, allows to plug a custom reducer that can
     * reduce the array of result objects into another response object.
     */
    public void setRemoteResultReducer(RemoteResultReducer remoteResultReducer) {
        this.remoteResultReducer = remoteResultReducer;
    }

    /**
     * When set to <code>true</code> (defaults to <code>true</code>) will return the first result
     * when using broadcast. If set to <code>false</code>, an array of results will be retuned.
     */
    public void setReturnFirstResult(boolean returnFirstResult) {
        this.returnFirstResult = returnFirstResult;
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

        String lookupName = getServiceInterface().getName();
        String methodName = methodInvocation.getMethod().getName();

        SyncSpaceRemotingEntry remotingEntry = new SyncSpaceRemotingEntry().buildInvocation(lookupName, methodName,
                methodInvocation.getArguments());

        if (!broadcast) {
            if (remoteRoutingHandler != null) {
                remotingEntry.setRouting(remoteRoutingHandler.computeRouting(remotingEntry));
            }
            if (remotingEntry.routing == null) {
                remotingEntry.routing = remotingEntry.hashCode();
            }
        }

        // check if this invocation will be a one way invocation
        if (globalOneWay) {
            remotingEntry.oneWay = Boolean.TRUE;
        } else {
            if (voidOneWay && methodInvocation.getMethod().getReturnType() == void.class) {
                remotingEntry.oneWay = Boolean.TRUE;
            }
        }

        if (remotingEntry.oneWay != null && remotingEntry.oneWay) {
            gigaSpace.readMultiple(remotingEntry, Integer.MAX_VALUE);
            return null;
        }

        Object[] result = gigaSpace.takeMultiple(remotingEntry, Integer.MAX_VALUE);
        if (result == null || result.length == 0) {
            throw new RemoteAccessException("Failed to invoke remoting service [" + remotingEntry + "], no return value");
        }
        if (remoteResultReducer != null) {
            SpaceRemotingResult[] results = new SpaceRemotingResult[result.length];
            System.arraycopy(result, 0, results, 0, result.length);
            return remoteResultReducer.reduce(results, remotingEntry);
        } else if (returnFirstResult) {
            SyncSpaceRemotingEntry resultEntry = (SyncSpaceRemotingEntry) result[0];
            if (resultEntry.ex != null) {
                throw resultEntry.ex;
            }
            return resultEntry.result;
        } else {
            Object[] retVals = new Object[result.length];
            for (int i = 0; i < result.length; i++) {
                SpaceRemotingResult spaceRemotingResult = (SpaceRemotingResult) result[i];
                if (spaceRemotingResult.getException() != null) {
                    throw spaceRemotingResult.getException();
                } else {
                    retVals[i] = ((SpaceRemotingResult) retVals[i]).getResult();
                }
            }
            return retVals;
        }
    }
}
