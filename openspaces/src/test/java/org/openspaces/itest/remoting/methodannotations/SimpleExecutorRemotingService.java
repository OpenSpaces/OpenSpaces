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

import org.openspaces.remoting.ExecutorRemotingMethod;

import java.util.concurrent.Future;

/**
 * @author uri
 */
public interface SimpleExecutorRemotingService {

    @ExecutorRemotingMethod(broadcast = true, remoteResultReducer = "sumResultReducer")
    int sumWithInjectedReducer(int value);

    @ExecutorRemotingMethod(broadcast = true, remoteResultReducer = "sumResultReducer")
    Future<Integer> asyncSumWithInjectedReducer(int value);

    @ExecutorRemotingMethod(broadcast = true, remoteResultReducerType = SumResultReducer.class)
    int sumWithReducerType(int value);

    @ExecutorRemotingMethod(broadcast = true, remoteResultReducerType = SumResultReducer.class)
    Future<Integer> asyncSumWithReducerType(int value);

    @ExecutorRemotingMethod(broadcast = false, remoteRoutingHandler = "constantRoutingHandler")
    int testInjectedRoutingHandler(int value);

    @ExecutorRemotingMethod(broadcast = false, remoteRoutingHandler = "constantRoutingHandler")
    Future<Integer> asyncTestInjectedRoutingHandler(int value);

    @ExecutorRemotingMethod(broadcast = false, remoteRoutingHandlerType = ConstantRoutingHandler.class)
    int testRoutingHandlerType(int value);

    @ExecutorRemotingMethod(broadcast = false, remoteRoutingHandlerType = ConstantRoutingHandler.class)
    Future<Integer> asyncTestRoutingHandlerType(int value);

    @ExecutorRemotingMethod(broadcast = false, remoteInvocationAspect = "returnTrueRemoteInvocationAspect")
    boolean testInjectedInvocationAspect();

    @ExecutorRemotingMethod(broadcast = false, remoteInvocationAspectType = ReturnTrueRemoteInvocationAspect.class)
    boolean testInvocationAspectType();

    @ExecutorRemotingMethod(broadcast = false, metaArgumentsHandler = "singleValueMetaArgumentsHandler")
    boolean testInjectedMetaArgumentsHandler();

    @ExecutorRemotingMethod(broadcast = false, metaArgumentsHandler = "singleValueMetaArgumentsHandler")
    Future<Boolean> asyncTestInjectedMetaArgumentsHandler();

    @ExecutorRemotingMethod(broadcast = false, metaArgumentsHandlerType = SingleValueMetaArgumentsHandler.class)
    boolean testMetaArgumentsHandlerType();

    @ExecutorRemotingMethod(broadcast = false, metaArgumentsHandlerType = SingleValueMetaArgumentsHandler.class)
    Future<Boolean> asyncTestMetaArgumentsHandlerType();
}
