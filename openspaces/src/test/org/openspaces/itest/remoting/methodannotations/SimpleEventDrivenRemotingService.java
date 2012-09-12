/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.remoting.methodannotations;

import org.openspaces.remoting.EventDrivenRemotingMethod;

import java.util.concurrent.Future;

/**
 * @author uri
 */
public interface SimpleEventDrivenRemotingService {


    @EventDrivenRemotingMethod(remoteRoutingHandler = "constantRoutingHandler")
    int testInjectedRoutingHandler(int value);

    @EventDrivenRemotingMethod(remoteRoutingHandler = "constantRoutingHandler")
    Future<Integer> asyncTestInjectedRoutingHandler(int value);

    @EventDrivenRemotingMethod(remoteRoutingHandlerType = ConstantRoutingHandler.class)
    int testRoutingHandlerType(int value);

    @EventDrivenRemotingMethod(remoteRoutingHandlerType = ConstantRoutingHandler.class)
    Future<Integer> asyncTestRoutingHandlerType(int value);

    @EventDrivenRemotingMethod(remoteInvocationAspect = "returnTrueRemoteInvocationAspect", remoteRoutingHandler = "constantRoutingHandler")
    boolean testInjectedInvocationAspect();

    @EventDrivenRemotingMethod(remoteInvocationAspectType = ReturnTrueRemoteInvocationAspect.class, remoteRoutingHandler = "constantRoutingHandler")
    boolean testInvocationAspectType();

    @EventDrivenRemotingMethod(metaArgumentsHandler = "singleValueMetaArgumentsHandler", remoteRoutingHandler = "constantRoutingHandler")
    boolean testInjectedMetaArgumentsHandler();

    @EventDrivenRemotingMethod(metaArgumentsHandler = "singleValueMetaArgumentsHandler", remoteRoutingHandler = "constantRoutingHandler")
    Future<Boolean> asyncTestInjectedMetaArgumentsHandler();

    @EventDrivenRemotingMethod(metaArgumentsHandlerType = SingleValueMetaArgumentsHandler.class, remoteRoutingHandler = "constantRoutingHandler")
    boolean testMetaArgumentsHandlerType();

    @EventDrivenRemotingMethod(metaArgumentsHandlerType = SingleValueMetaArgumentsHandler.class, remoteRoutingHandler = "constantRoutingHandler")
    Future<Boolean> asyncTestMetaArgumentsHandlerType();
}
