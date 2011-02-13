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
public class EventDrivenRemotingMethodAnnotationsTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleEventDrivenRemotingService eventDrivenProxy;

    protected GigaSpace gigaSpace;

    public EventDrivenRemotingMethodAnnotationsTests() {
        setPopulateProtectedVariables(true);
        setAutowireMode(AUTOWIRE_BY_NAME);
    }


    @Override
    protected String[] getConfigLocations() {
        return new String[] {"/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting.xml"};
    }



    public void testTakeMultipleWorking() {
        gigaSpace.write(new Message(1));
        gigaSpace.write(new Message(2));
        Message[] result = gigaSpace.takeMultiple(new Message(), Integer.MAX_VALUE);
        assertEquals(2, result.length);
        gigaSpace.clear(new Message());
    }

    public void testInjectedRoutingHandler() {
        int value = eventDrivenProxy.testInjectedRoutingHandler(1);
        assertEquals(value, 1);
    }

    public void testAsyncInjectedRoutingHandler() throws ExecutionException, InterruptedException {
        Future<Integer> result = eventDrivenProxy.asyncTestInjectedRoutingHandler(1);
        assertEquals(result.get(), new Integer(1));
    }

    public void testRoutingHandlerType() {
        int value = eventDrivenProxy.testRoutingHandlerType(1);
        assertEquals(value, 1);
    }

    public void testAsyncRoutingHandlerType() throws ExecutionException, InterruptedException {
        Future<Integer> result = eventDrivenProxy.asyncTestRoutingHandlerType(1);
        assertEquals(result.get(), new Integer(1));
    }

    public void testInjectedInvocationAspect() {
        boolean value = eventDrivenProxy.testInjectedInvocationAspect();
        assertTrue(value);
    }

    public void testInvocationAspectType() {
        boolean value = eventDrivenProxy.testInvocationAspectType();
        assertTrue(value);
    }

    public void testInjectedMetaArgumentsHandler() {
        boolean value = eventDrivenProxy.testInjectedMetaArgumentsHandler();
        assertTrue(value);
    }

    public void testAsyncInjectedMetaArgumentsHandler() throws ExecutionException, InterruptedException {
        Future<Boolean> result = eventDrivenProxy.asyncTestInjectedMetaArgumentsHandler();
        assertEquals(result.get(), Boolean.TRUE);
    }


    public void testMetaArgumentsHandlerType() {
        boolean value = eventDrivenProxy.testMetaArgumentsHandlerType();
        assertTrue(value);
    }

    public void testAsyncMetaArgumentsHandlerType() throws ExecutionException, InterruptedException {
        Future<Boolean> result = eventDrivenProxy.asyncTestMetaArgumentsHandlerType();
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