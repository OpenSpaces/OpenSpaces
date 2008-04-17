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
 * A simple programmatic configurer creating a remote asyncronous proxy
 *
 * <p>Usage example:
 * <pre>
 * IJSpace space = new UrlSpaceConfigurer("jini://&#42;/&#42;/mySpace")
 *                        .space();
 * GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
 * MyBusinessInterface proxy = new AsyncRemotingProxyConfigurer&lt;MyBusinessInterface&gt;(gigaSpace, MyBusinessInterface.class)
 *                                  .timeout(15000)
 *                                  .asyncProxy();
 * proxy.businessMethod(...);
 * </pre>
 *
 * @author Uri Cohen
 */
public class AsyncRemotingProxyConfigurer<T> {

    private AsyncSpaceRemotingProxyFactoryBean asyncFactoryBean;

    public AsyncRemotingProxyConfigurer(GigaSpace gigaSpace, Class<T> serviceInterface) {
        asyncFactoryBean = new AsyncSpaceRemotingProxyFactoryBean();
        asyncFactoryBean.setGigaSpace(gigaSpace);
        asyncFactoryBean.setServiceInterface(serviceInterface);
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#setGlobalOneWay(boolean)  
     */
    public AsyncRemotingProxyConfigurer<T> globalOneWay(boolean globalOneWay) {
        asyncFactoryBean.setGlobalOneWay(globalOneWay);
        return this;
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#setFifo(boolean)
     */
    public AsyncRemotingProxyConfigurer<T> fifo(boolean fifo) {
        asyncFactoryBean.setFifo(fifo);
        return this;
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#setMetaArgumentsHandler(MetaArgumentsHandler)
     */
    public AsyncRemotingProxyConfigurer<T> metaArgumentsHandler(MetaArgumentsHandler metaArgumentsHandler) {
        asyncFactoryBean.setMetaArgumentsHandler(metaArgumentsHandler);
        return this;
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#remoteInvocationAspect 
     */
    public AsyncRemotingProxyConfigurer<T> remoteInvocationAspect(RemoteInvocationAspect remoteInvocationAspect) {
        asyncFactoryBean.setRemoteInvocationAspect(remoteInvocationAspect);
        return this;
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#setVoidOneWay(boolean)
     */
    public AsyncRemotingProxyConfigurer<T> voidOneWay(boolean voidOneWay) {
        asyncFactoryBean.setVoidOneWay(voidOneWay);
        return this;
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#setTimeout(long)
     */
    public AsyncRemotingProxyConfigurer<T> timeout(long timeout) {
        asyncFactoryBean.setTimeout(timeout);
        return this;
    }

    /**
     * @see org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean#setRemoteRoutingHandler(RemoteRoutingHandler)
     */
    public AsyncRemotingProxyConfigurer<T> remoteRoutingHandler(RemoteRoutingHandler remoteRoutingHandler) {
        asyncFactoryBean.setRemoteRoutingHandler(remoteRoutingHandler);
        return this;
    }

    /**
     * Creates a new asynchronous proxy of type T
     */
    public T asyncProxy(){
        asyncFactoryBean.afterPropertiesSet();
        return (T) asyncFactoryBean.getObject();
    }


}
