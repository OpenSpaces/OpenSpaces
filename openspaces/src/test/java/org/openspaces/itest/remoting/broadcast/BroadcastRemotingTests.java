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

package org.openspaces.itest.remoting.broadcast;

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
import static org.junit.Assert.fail;

/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/broadcast/broadcast-remoting.xml")
public class BroadcastRemotingTests   { 

     @Autowired protected SimpleService executorService;

     @Autowired protected GigaSpace gigaSpace;

    public BroadcastRemotingTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/broadcast/broadcast-remoting.xml"};
    }

     @Test public void testTakeMultipleWorking() {
        gigaSpace.write(new Message(1));
        gigaSpace.write(new Message(2));
        Message[] result = gigaSpace.takeMultiple(new Message(), Integer.MAX_VALUE);
        assertEquals(2, result.length);
        gigaSpace.clear(new Message());
    }

     @Test public void testExecutorSyncBroadcast() {
        innerTestSyncBroadcast(executorService);
    }

//     @Test public void testSyncSyncBroadcast() {
//        innerTestSyncBroadcast(syncService);
//    }

     @Test public void testExecutorAsyncBroadcast() throws ExecutionException, InterruptedException {
        innerTestAsyncBroadcast(executorService);
    }

//     @Test public void testSyncAsyncBroadcast() throws ExecutionException, InterruptedException {
//        innerTestAsyncBroadcast(syncService);
//    }

     @Test public void testExecutorAsyncException() throws ExecutionException, InterruptedException {
        innerTestAsyncException(executorService);
    }

     @Test public void testExecutorSyncException() throws ExecutionException, InterruptedException {
        innerTestSyncException(executorService);
    }

    private void innerTestSyncBroadcast(SimpleService service) {
        int value = service.sum(2);
        assertEquals(4, value);
    }

    private void innerTestAsyncBroadcast(SimpleService service) throws ExecutionException, InterruptedException {
        Future<Integer> future = service.asyncSum(2);
        assertEquals(4, (int) future.get());
    }

    public void innerTestSyncException(SimpleService service) {
        try {
            service.testException();
            fail();
        } catch (SimpleService.MyException e) {
            // all is well
        }
    }

    public void innerTestAsyncException(SimpleService service) {
        try {
            service.asyncTestException().get();
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SimpleService.MyException)) {
                fail();
            }
        }
    }

    @SpaceClass
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/broadcast/broadcast-remoting.xml")
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

