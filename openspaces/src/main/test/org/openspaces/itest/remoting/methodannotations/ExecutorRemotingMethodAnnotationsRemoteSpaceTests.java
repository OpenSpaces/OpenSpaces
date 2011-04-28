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

package org.openspaces.itest.remoting.methodannotations;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author uri
 */
public class ExecutorRemotingMethodAnnotationsRemoteSpaceTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleExecutorRemotingService executorProxy;

    protected GigaSpace remoteGigaSpace;

    public ExecutorRemotingMethodAnnotationsRemoteSpaceTests() {
        setPopulateProtectedVariables(true);
    }


    @Override
    protected String[] getConfigLocations() {
        return new String[] {"/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting-remote-space.xml"};
    }



    public void testTakeMultipleWorking() {
        remoteGigaSpace.write(new Message(1));
        remoteGigaSpace.write(new Message(2));
        Message[] result = remoteGigaSpace.takeMultiple(new Message(), Integer.MAX_VALUE);
        assertEquals(2, result.length);
        remoteGigaSpace.clear(new Message());
    }

    public void testExecutorBroadcastWithInjectedReducer() {
        int value = executorProxy.sumWithInjectedReducer(2);
        assertEquals(4, value);
    }

    public void testExecutorAsyncBroadcastWithInjectedReducer() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncSumWithInjectedReducer(2);
        assertEquals(new Integer(4), result.get());
    }

    public void testExecutorBroadcastWithReducerType() {
        int value = executorProxy.sumWithReducerType(2);
        assertEquals(4, value);
    }

    public void testAsyncExecutorBroadcastWithReducerType() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncSumWithReducerType(2);
        assertEquals(new Integer(4), result.get());
    }

    public void testInjectedRoutingHandler() {
        int value = executorProxy.testInjectedRoutingHandler(1);
        assertEquals(value, 1);
    }

    public void testAsyncInjectedRoutingHandler() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncTestInjectedRoutingHandler(1);
        assertEquals(result.get(), new Integer(1));
    }

    public void testRoutingHandlerType() {
        int value = executorProxy.testRoutingHandlerType(1);
        assertEquals(value, 1);
    }

    public void testAsyncRoutingHandlerType() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncTestRoutingHandlerType(1);
        assertEquals(result.get(), new Integer(1));
    }

    public void testInjectedInvocationAspect() {
        boolean value = executorProxy.testInjectedInvocationAspect();
        assertTrue(value);
    }

    public void testInvocationAspectType() {
        boolean value = executorProxy.testInvocationAspectType();
        assertTrue(value);
    }

    public void testInjectedMetaArgumentsHandler() {
        boolean value = executorProxy.testInjectedMetaArgumentsHandler();
        assertTrue(value);
    }

    public void testAsyncInjectedMetaArgumentsHandler() throws ExecutionException, InterruptedException {
        Future<Boolean> result = executorProxy.asyncTestInjectedMetaArgumentsHandler();
        assertEquals(result.get(), Boolean.TRUE);
    }


    public void testMetaArgumentsHandlerType() {
        boolean value = executorProxy.testMetaArgumentsHandlerType();
        assertTrue(value);
    }

    public void testAsyncMetaArgumentsHandlerType() throws ExecutionException, InterruptedException {
        Future<Boolean> result = executorProxy.asyncTestMetaArgumentsHandlerType();
        assertEquals(result.get(), Boolean.TRUE);
    }



    @SpaceClass
    public static class Message {

        private Integer routing;

        public Message() {
        }

        public Message(int routing) {
            this.routing = routing;
        }

        @SpaceRouting
        public Integer getRouting() {
            return routing;
        }

        public void setRouting(Integer routing) {
            this.routing = routing;
        }
    }
}