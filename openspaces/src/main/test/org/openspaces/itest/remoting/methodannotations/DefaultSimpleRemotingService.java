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

import java.util.concurrent.Future;

/**
 * @author uri
 */
public class DefaultSimpleRemotingService implements SimpleExecutorRemotingService, SimpleEventDrivenRemotingService {

    public int sumWithInjectedReducer(int value) {
        return value;
    }

    public Future<Integer> asyncSumWithInjectedReducer(int value) {
        return null;
    }

    public int sumWithReducerType(int value) {
        return value;
    }

    public Future<Integer> asyncSumWithReducerType(int value) {
        return null;
    }

    public int testInjectedRoutingHandler(int value) {
        return value;
    }

    public Future<Integer> asyncTestInjectedRoutingHandler(int value) {
        return null;
    }

    public int testRoutingHandlerType(int value) {
        return value;
    }

    public Future<Integer> asyncTestRoutingHandlerType(int value) {
        return null;
    }

    public boolean testInjectedInvocationAspect() {
        return false;
    }

    public boolean testInvocationAspectType() {
        return false;
    }

    public boolean testInjectedMetaArgumentsHandler() {
        return false;
    }

    public Future<Boolean> asyncTestInjectedMetaArgumentsHandler() {
        return null;
    }

    public boolean testMetaArgumentsHandlerType() {
        return false;
    }

    public Future<Boolean> asyncTestMetaArgumentsHandlerType() {
        return null;
    }
}
