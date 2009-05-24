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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to inject {@link SyncSpaceRemotingProxyFactoryBean}
 * into a field.
 *
 * @author kimchy
 * @see SyncSpaceRemotingProxyFactoryBean
 * @see org.openspaces.remoting.RemotingAnnotationBeanPostProcessor
 * @deprecated Use {@link org.openspaces.remoting.ExecutorProxy} and executor based remoting instead.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncProxy {

    /**
     * The name of the {@link org.openspaces.core.GigaSpace} instance (representing the Space)
     * that this remote invocation will occur on.
     *
     * <p>If there is only one instance of {@link org.openspaces.core.GigaSpace}, will defualt to
     * it. If not, will throw an exception if not defined.
     */
    String gigaSpace() default "";

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setGlobalOneWay(boolean) 
     */
    boolean globablOneWay() default false;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setVoidOneWay(boolean)
     */
    boolean voidOneWay() default false;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setBroadcast(boolean) 
     */
    boolean broadcast() default false;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setRemoteRoutingHandler(RemoteRoutingHandler)
     */
    String remoteRoutingHandler() default "";

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setRemoteRoutingHandler(RemoteRoutingHandler)
     */
    Class remoteRoutingHandlerType() default Object.class;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setMetaArgumentsHandler(MetaArgumentsHandler)
     */
    String metaArgumentsHandler() default "";

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setMetaArgumentsHandler(MetaArgumentsHandler)
     */
    Class metaArgumentsHandlerType() default Object.class;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setRemoteInvocationAspect(RemoteInvocationAspect)
     */
    String remoteInvocationAspect() default "";

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setRemoteInvocationAspect(RemoteInvocationAspect)
     */
    Class remoteInvocationAspectType() default Object.class;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setRemoteResultReducer(RemoteResultReducer) 
     */
    String remoteResultReducer() default "";

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setRemoteResultReducer(RemoteResultReducer)
     */
    Class remoteResultReducerType() default Object.class;

    /**
     * @see org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean#setReturnFirstResult(boolean) 
     */
    boolean returnFirstResult() default true;
}