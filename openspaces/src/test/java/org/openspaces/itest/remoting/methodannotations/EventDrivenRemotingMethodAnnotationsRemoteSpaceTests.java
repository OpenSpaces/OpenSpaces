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
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author uri
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting-remote-space.xml")
public class EventDrivenRemotingMethodAnnotationsRemoteSpaceTests   { 

     @Autowired protected SimpleEventDrivenRemotingService eventDrivenProxy;

     @Autowired protected GigaSpace remoteGigaSpace;

    public EventDrivenRemotingMethodAnnotationsRemoteSpaceTests() {
 
 
    }


    //@Override
    protected String[] getConfigLocations () {
        return new String[] {"/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting-remote-space.xml"};
    }



     @Test public void testTakeMultipleWorking() {
        remoteGigaSpace.write(new Message(1));
        remoteGigaSpace.write(new Message(2));
        Message[] result = remoteGigaSpace.takeMultiple(new Message(), Integer.MAX_VALUE);
        assertEquals(2, result.length);
        remoteGigaSpace.clear(new Message());
    }

     @Test public void testInjectedRoutingHandler() {
        for (int i = 0; i < 10000; i++) {
            int value = eventDrivenProxy.testInjectedRoutingHandler(1);
            assertEquals(value, 1);
        }
    }

     @Test public void testAsyncInjectedRoutingHandler() throws ExecutionException, InterruptedException {
        Future<Integer> result = eventDrivenProxy.asyncTestInjectedRoutingHandler(1);
        assertEquals(result.get(), new Integer(1));
    }

     @Test public void testRoutingHandlerType() {
        int value = eventDrivenProxy.testRoutingHandlerType(1);
        assertEquals(value, 1);
    }

     @Test public void testAsyncRoutingHandlerType() throws ExecutionException, InterruptedException {
        Future<Integer> result = eventDrivenProxy.asyncTestRoutingHandlerType(1);
        assertEquals(result.get(), new Integer(1));
    }

     @Test public void testInjectedInvocationAspect() {
        boolean value = eventDrivenProxy.testInjectedInvocationAspect();
        assertTrue(value);
    }

     @Test public void testInvocationAspectType() {
        boolean value = eventDrivenProxy.testInvocationAspectType();
        assertTrue(value);
    }

     @Test public void testInjectedMetaArgumentsHandler() {
        boolean value = eventDrivenProxy.testInjectedMetaArgumentsHandler();
        assertTrue(value);
    }

     @Test public void testAsyncInjectedMetaArgumentsHandler() throws ExecutionException, InterruptedException {
        Future<Boolean> result = eventDrivenProxy.asyncTestInjectedMetaArgumentsHandler();
        assertEquals(result.get(), Boolean.TRUE);
    }


     @Test public void testMetaArgumentsHandlerType() {
        boolean value = eventDrivenProxy.testMetaArgumentsHandlerType();
        assertTrue(value);
    }

     @Test public void testAsyncMetaArgumentsHandlerType() throws ExecutionException, InterruptedException {
        Future<Boolean> result = eventDrivenProxy.asyncTestMetaArgumentsHandlerType();
        assertEquals(result.get(), Boolean.TRUE);
    }



    @SpaceClass
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting-remote-space.xml")
    public static class Message  { 

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
        
        private String uid;

        @SpaceId(autoGenerate=true)
        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}

