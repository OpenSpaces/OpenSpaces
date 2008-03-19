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

import org.openspaces.core.GigaSpace;

/**
 * A simple programmatic configurer creating a remote syncronous proxy
 *
 * <p>Usage example:
 * <pre>
 * IPojoSpace space = new UrlSpaceConfigurer("jini://&#42;/&#42;/mySpace")
 *                        .space();
 * GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
 * MyBusinessInterface proxy = new SyncRemotingProxyConfigurer&lt;MyBusinessInterface&gt;(gigaSpace, MyBusinessInterface.class)
 *                                         .broadcast(true)
 *                                         .voidOneWay(true)
 *                                         .syncProxy();
 * proxy.businessMethod(...);
 * </pre>
 *
 * @author uri
 */
public class SyncRemotingProxyConfigurer<T> {

    private SyncSpaceRemotingProxyFactoryBean syncFactoryBean;

    public SyncRemotingProxyConfigurer(GigaSpace gigaSpace, Class<T> serviceInterface) {
        syncFactoryBean = new SyncSpaceRemotingProxyFactoryBean();
        syncFactoryBean.setGigaSpace(gigaSpace);
        syncFactoryBean.setServiceInterface(serviceInterface);
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setBroadcast(boolean) 
     */
    public SyncRemotingProxyConfigurer<T> broadcast(boolean broadcat) {
        syncFactoryBean.setBroadcast(broadcat);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setBroadcast(boolean)
     * @see SyncSpaceRemotingProxyFactoryBean#setRemoteResultReducer(RemoteResultReducer) 
     */
    public <X,Y> SyncRemotingProxyConfigurer<T> broadcast(RemoteResultReducer<X,Y> remoteResultReducer) {
        syncFactoryBean.setRemoteResultReducer(remoteResultReducer);
        syncFactoryBean.setBroadcast(true);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setRemoteResultReducer(RemoteResultReducer)  
     */
    public SyncRemotingProxyConfigurer<T> remoteResultReducer(RemoteResultReducer remoteResultReducer) {
        syncFactoryBean.setRemoteResultReducer(remoteResultReducer);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setReturnFirstResult(boolean)  
     */
    public SyncRemotingProxyConfigurer<T> returnFirstResult(boolean returnFirstResult) {
        syncFactoryBean.setReturnFirstResult(returnFirstResult);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setGlobalOneWay(boolean)
     */
    public SyncRemotingProxyConfigurer<T> globalOneWay(boolean globalOneWay) {
        syncFactoryBean.setGlobalOneWay(globalOneWay);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setMetaArgumentsHandler(org.openspaces.remoting.MetaArgumentsHandler)
     */
    public SyncRemotingProxyConfigurer<T> metaArgumentsHandler(MetaArgumentsHandler metaArgumentsHandler) {
        syncFactoryBean.setMetaArgumentsHandler(metaArgumentsHandler);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#remoteInvocationAspect
     */
    public SyncRemotingProxyConfigurer<T> remoteInvocationAspect(RemoteInvocationAspect remoteInvocationAspect) {
        syncFactoryBean.setRemoteInvocationAspect(remoteInvocationAspect);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setVoidOneWay(boolean)
     */
    public SyncRemotingProxyConfigurer<T> voidOneWay(boolean voidOneWay) {
        syncFactoryBean.setVoidOneWay(voidOneWay);
        return this;
    }

    /**
     * @see SyncSpaceRemotingProxyFactoryBean#setRemoteRoutingHandler(org.openspaces.remoting.RemoteRoutingHandler)
     */
    public SyncRemotingProxyConfigurer<T> remoteRoutingHandler(RemoteRoutingHandler remoteRoutingHandler) {
        syncFactoryBean.setRemoteRoutingHandler(remoteRoutingHandler);
        return this;
    }

    /**
     * Creates a new synchronous proxy of type T
     */
    public T syncProxy(){
        syncFactoryBean.afterPropertiesSet();
        return (T) syncFactoryBean.getObject();
    }


}